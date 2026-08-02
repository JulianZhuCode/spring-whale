package io.github.springwhale.platform.task.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.dto.vo.TaskVO;
import io.github.springwhale.platform.task.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.handler.TaskHandler;
import io.github.springwhale.platform.task.mapper.TaskMapper;
import io.github.springwhale.platform.task.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.repository.TaskBatchRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core service for batch task management.
 * <p>
 * Provides task lifecycle management: creation, execution (with concurrency),
 * pause, resume (breakpoint), cancel, and progress tracking.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

    private static final int DEFAULT_CONCURRENCY = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
    private static final int STATUS_CHECK_INTERVAL = 100;
    private final TaskBatchRepository taskRepository;
    private final TaskBatchItemRepository itemRepository;
    private final TaskMapper taskMapper;
    private final List<TaskHandler> handlers;
    private final PlatformTransactionManager transactionManager;
    private final Map<String, Future<?>> runningFutures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .findAndRegisterModules();
    private Map<String, TaskHandler> handlerMap;
    private ExecutorService taskExecutor;

    @PostConstruct
    public void init() {
        this.handlerMap = new HashMap<>();
        for (TaskHandler h : handlers) {
            this.handlerMap.put(h.getTaskType(), h);
        }
        this.taskExecutor = new ThreadPoolExecutor(
                DEFAULT_CONCURRENCY,
                DEFAULT_CONCURRENCY,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r, "task-executor");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("TaskService initialized: {} task threads", DEFAULT_CONCURRENCY);
    }

    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdownNow();
        }
    }

    private TaskHandler getHandler(String taskType) {
        TaskHandler handler = handlerMap.get(taskType);
        if (handler == null) {
            throw BusinessException.create("TASK_TYPE_NOT_FOUND", "No handler registered for task type: " + taskType);
        }
        return handler;
    }

    // ==================== Task Lifecycle ====================

    /**
     * Creates a new batch task with all items enumerated and persisted.
     * <p>
     * Exclusivity: only one active task (PENDING, RUNNING, PAUSED) per task type is allowed.
     * If an active task of the same type exists, returns the existing task instead of creating a new one.
     */
    @Transactional
    public TaskVO create(TaskCreateRequest request) {
        TaskHandler handler = getHandler(request.getTaskType());

        List<TaskStatus> terminalStatuses = List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED, TaskStatus.FAILED);
        List<TaskBatchEntity> existing = taskRepository.findByTaskTypeAndStatusNotIn(
                request.getTaskType(), terminalStatuses);
        if (!existing.isEmpty()) {
            TaskBatchEntity activeTask = existing.get(0);
            log.info("Task type [{}] already has an active task [{}], returning existing",
                    request.getTaskType(), activeTask.getId());
            return toVO(activeTask);
        }

        Map<String, Object> params = request.getParams() != null ? request.getParams() : new HashMap<>();

        TaskBatchEntity task = new TaskBatchEntity();
        task.setTaskType(request.getTaskType());
        task.setStatus(TaskStatus.PENDING);
        task.setParams(toJson(params));

        List<String> itemKeys = handler.enumerateItems(params);
        task.setTotalCount(itemKeys.size());

        task = taskRepository.save(task);

        List<TaskBatchItemEntity> items = new ArrayList<>();
        for (String key : itemKeys) {
            TaskBatchItemEntity item = new TaskBatchItemEntity();
            item.setTaskId(task.getId());
            item.setItemKey(key);
            item.setStatus(TaskItemStatus.PENDING);
            items.add(item);
        }
        itemRepository.saveAll(items);

        log.info("Created task [{}] type={}, items={}", task.getId(), request.getTaskType(), itemKeys.size());
        return toVO(task);
    }

    /**
     * Starts executing a task. If the task is PAUSED or has RUNNING items,
     * it resumes from the breakpoint (skipping already-successful items).
     */
    @Transactional
    public TaskVO start(Integer taskId) {
        log.info("start() called for taskId={}", taskId);
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() == TaskStatus.RUNNING) {
            log.warn("Task [{}] is already RUNNING, rejecting start", taskId);
            throw BusinessException.create("TASK_ALREADY_RUNNING", "Task is already running: " + taskId);
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            log.warn("Task [{}] is already finished (status={}), rejecting start", taskId, task.getStatus());
            throw BusinessException.create("TASK_FINISHED", "Task is already finished: " + taskId);
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        log.info("Task [{}] start: handler={}, params={}", taskId, handler.getTaskType(), params);
        handler.beforeStart(params);

        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(task.getStartTime() != null ? task.getStartTime() : LocalDateTime.now());
        taskRepository.save(task);
        log.info("Task [{}] status set to RUNNING, submitting for execution after commit", taskId);

        submitAfterCommit(taskId, handler, params);

        return toVO(task);
    }

    /**
     * Pauses a running task. The current in-flight items will complete,
     * but no new items will be picked up.
     */
    @Transactional
    public TaskVO pause(Integer taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.RUNNING) {
            throw BusinessException.create("TASK_NOT_RUNNING", "Task is not running: " + taskId);
        }

        task.setStatus(TaskStatus.PAUSED);
        taskRepository.save(task);

        Future<?> future = runningFutures.get(String.valueOf(taskId));
        if (future != null) {
            future.cancel(true);
        }

        log.info("Paused task [{}]", taskId);
        return toVO(task);
    }

    /**
     * Resumes a paused task from the breakpoint.
     */
    @Transactional
    public TaskVO resume(Integer taskId) {
        log.info("resume() called for taskId={}", taskId);
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.PAUSED) {
            log.warn("Task [{}] is not PAUSED (status={}), rejecting resume", taskId, task.getStatus());
            throw BusinessException.create("TASK_NOT_PAUSED", "Task is not paused: " + taskId);
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        log.info("Task [{}] resume: handler={}, params={}, success={}, fail={}, total={}",
                taskId, handler.getTaskType(), params,
                task.getSuccessCount(), task.getFailCount(), task.getTotalCount());

        task.setStatus(TaskStatus.RUNNING);
        taskRepository.save(task);
        log.info("Task [{}] status set to RUNNING, submitting for execution after commit", taskId);

        submitAfterCommit(taskId, handler, params);

        return toVO(task);
    }

    /**
     * Cancels a task. In-flight items may still complete, but no new items are picked up.
     */
    @Transactional
    public TaskVO cancel(Integer taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            return toVO(task);
        }

        if (task.getStatus() == TaskStatus.RUNNING) {
            Future<?> future = runningFutures.get(String.valueOf(taskId));
            if (future != null) {
                future.cancel(true);
            }
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());
        handler.onCancel(params);

        task.setStatus(TaskStatus.CANCELLED);
        task.setEndTime(LocalDateTime.now());
        taskRepository.save(task);

        cleanupTerminalItems(taskId);

        log.info("Cancelled task [{}]", taskId);
        return toVO(task);
    }

    /**
     * Deletes a terminal task (COMPLETED / CANCELLED / FAILED).
     * Running / Paused tasks cannot be deleted; use cancel first.
     */
    @Transactional
    public void delete(Integer taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() == TaskStatus.RUNNING || task.getStatus() == TaskStatus.PAUSED
                || task.getStatus() == TaskStatus.PENDING) {
            throw BusinessException.create("TASK_ACTIVE",
                    "Cannot delete active task (status=" + task.getStatus() + "), cancel or stop it first");
        }

        itemRepository.deleteByTaskId(taskId);
        taskRepository.delete(task);
        log.info("Deleted task [{}] (status={})", taskId, task.getStatus());
    }

    // ==================== Query Methods ====================

    public Optional<TaskVO> findById(Integer id) {
        return taskRepository.findById(id).map(this::toVO);
    }

    /**
     * 终态清理：删除已完成的 items（SUCCESS/SKIPPED），保留 FAILED 供重试。
     * 应在任务进入终态（COMPLETED/CANCELLED/FAILED）的事务内调用。
     */
    private void cleanupTerminalItems(Integer taskId) {
        itemRepository.deleteByTaskIdAndStatusIn(taskId,
                List.of(TaskItemStatus.SUCCESS, TaskItemStatus.SKIPPED));
        log.info("Cleaned up SUCCESS/SKIPPED items for task [{}]", taskId);
    }

    /**
     * Returns all failed items for a given task.
     */
    public List<TaskBatchItemEntity> findFailedItems(Integer taskId) {
        return itemRepository.findByTaskIdAndStatusOrderByIdAsc(taskId, TaskItemStatus.FAILED);
    }

    /**
     * Resets FAILED items back to PENDING and starts/resumes execution immediately.
     */
    @Transactional
    public TaskVO retryFailed(Integer taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.CANCELLED
                && task.getStatus() != TaskStatus.FAILED) {
            throw BusinessException.create("TASK_ACTIVE",
                    "Cannot retry items of an active task (status=" + task.getStatus() + ")");
        }

        List<TaskBatchItemEntity> failedItems = itemRepository
                .findByTaskIdAndStatusOrderByIdAsc(taskId, TaskItemStatus.FAILED);

        if (failedItems.isEmpty()) {
            throw BusinessException.create("NO_FAILED_ITEMS", "No failed items to retry");
        }

        int resetCount = 0;
        for (TaskBatchItemEntity item : failedItems) {
            item.setStatus(TaskItemStatus.PENDING);
            item.setErrorMessage(null);
            item.setRetryCount(item.getRetryCount() != null ? item.getRetryCount() + 1 : 1);
            resetCount++;
        }
        itemRepository.saveAll(failedItems);

        task.setEndTime(null);
        task.setFailCount(0);

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        if (task.getStartTime() == null) {
            handler.beforeStart(params);
            task.setStartTime(LocalDateTime.now());
        }
        task.setStatus(TaskStatus.RUNNING);
        taskRepository.save(task);

        log.info("Retry: reset {} failed items to PENDING for task [{}], failCount cleared, status set to RUNNING",
                resetCount, taskId);

        submitAfterCommit(taskId, handler, params);
        return toVO(task);
    }

    public Page<TaskVO> findAll(int page, int size, String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return taskRepository.findAll(pageable).map(this::toVO);
    }

    public Page<TaskVO> findByStatus(TaskStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return taskRepository.findByStatus(status, pageable).map(this::toVO);
    }

    public Page<TaskVO> findByTaskType(String taskType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return taskRepository.findByTaskType(taskType, pageable).map(this::toVO);
    }

    /**
     * Recovers tasks that were RUNNING at startup (likely interrupted).
     * Marks them as PAUSED so they can be manually resumed.
     */
    @Transactional
    public int recoverInterruptedTasks() {
        List<TaskBatchEntity> interrupted = taskRepository.findByStatusIn(List.of(TaskStatus.RUNNING));
        int count = 0;
        for (TaskBatchEntity task : interrupted) {
            task.setStatus(TaskStatus.PAUSED);
            taskRepository.save(task);
            count++;
            log.warn("Recovered interrupted task [{}], marked as PAUSED", task.getId());
        }
        return count;
    }

    // ==================== Execution Engine ====================

    private void submitAfterCommit(Integer taskId, TaskHandler handler, Map<String, Object> params) {
        log.info("submitAfterCommit() for taskId={}, transactionActive={}",
                taskId, TransactionSynchronizationManager.isActualTransactionActive());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("afterCommit() triggered for taskId={}", taskId);
                    TaskBatchEntity freshTask = taskRepository.findById(taskId).orElse(null);
                    if (freshTask != null && freshTask.getStatus() == TaskStatus.RUNNING) {
                        log.info("Task [{}] is RUNNING after commit, submitting for execution", taskId);
                        submitForExecution(freshTask, handler, params);
                    } else {
                        log.warn("Task [{}] not RUNNING after commit (status={}), skipping execution",
                                taskId, freshTask != null ? freshTask.getStatus() : "null");
                    }
                }
            });
        } else {
            log.info("No active transaction for taskId={}, submitting immediately", taskId);
            TaskBatchEntity freshTask = taskRepository.findById(taskId).orElse(null);
            if (freshTask != null && freshTask.getStatus() == TaskStatus.RUNNING) {
                submitForExecution(freshTask, handler, params);
            }
        }
    }

    private void submitForExecution(TaskBatchEntity task, TaskHandler handler, Map<String, Object> params) {
        log.info("submitForExecution() for taskId={}, handler={}, thread={}",
                task.getId(), handler.getTaskType(), Thread.currentThread().getName());
        Future<?> future = taskExecutor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("Task [{}] execution started on thread={}", task.getId(), threadName);
            try {
                executeTask(task, handler, params);
                log.info("Task [{}] execution completed on thread={}", task.getId(), threadName);
            } catch (Exception e) {
                log.error("Task [{}] execution failed with unexpected error", task.getId(), e);
                new TransactionTemplate(transactionManager).execute(status -> {
                    taskRepository.findById(task.getId()).ifPresent(t -> {
                        if (t.getStatus() == TaskStatus.RUNNING) {
                            t.setStatus(TaskStatus.FAILED);
                            t.setErrorMessage(e.getMessage());
                            t.setEndTime(LocalDateTime.now());
                            taskRepository.save(t);
                            log.warn("Task [{}] status set to FAILED due to exception: {}", task.getId(), e.getMessage());
                        } else {
                            log.info("Task [{}] status is {} (not RUNNING), skipping FAILED transition",
                                    task.getId(), t.getStatus());
                        }
                    });
                    cleanupTerminalItems(task.getId());
                    return null;
                });
            } finally {
                runningFutures.remove(String.valueOf(task.getId()));
                log.info("Task [{}] future removed from runningFutures", task.getId());
            }
        });
        runningFutures.put(String.valueOf(task.getId()), future);
        log.info("Task [{}] submitted to thread pool, future={}", task.getId(), future);
    }

    private void executeTask(TaskBatchEntity task, TaskHandler handler, Map<String, Object> params) {
        Integer taskId = task.getId();
        log.info("executeTask() started for taskId={}, thread={}", taskId, Thread.currentThread().getName());

        if (!taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
            log.info("Task [{}] is not RUNNING, skipping execution", taskId);
            return;
        }

        List<TaskBatchItemEntity> pendingItems = itemRepository
                .findByTaskIdAndStatusOrderByIdAsc(taskId, TaskItemStatus.PENDING);

        int total = task.getTotalCount();
        AtomicInteger successCount = new AtomicInteger(task.getSuccessCount() != null ? task.getSuccessCount() : 0);
        AtomicInteger failCount = new AtomicInteger(task.getFailCount() != null ? task.getFailCount() : 0);
        AtomicInteger skippedCount = new AtomicInteger(task.getSkippedCount() != null ? task.getSkippedCount() : 0);
        AtomicInteger completedCounter = new AtomicInteger(0);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        log.info("Task [{}]: starting {} pending items (total={}, success={}, fail={}, skipped={})",
                taskId, pendingItems.size(), total, successCount.get(), failCount.get(), skippedCount.get());

        Map<String, TaskBatchItemEntity> itemMap = new ConcurrentHashMap<>();
        List<String> itemKeys = new ArrayList<>(pendingItems.size());
        for (TaskBatchItemEntity item : pendingItems) {
            itemMap.put(item.getItemKey(), item);
            itemKeys.add(item.getItemKey());
        }

        Set<Integer> savedItemIds = ConcurrentHashMap.newKeySet();
        List<TaskBatchItemEntity> dirtyItems = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger flushCounter = new AtomicInteger(0);

        int progressUpdateInterval = Math.max(20, Math.min(100, pendingItems.size() / 50));

        TaskHandler.BatchProgressCallback callback = new TaskHandler.BatchProgressCallback() {
            @Override
            public void onItemResult(String itemKey, boolean success) {
                applyResult(itemKey, success, null);
            }

            @Override
            public void onItemResult(String itemKey, boolean success, String errorMessage) {
                applyResult(itemKey, success, errorMessage);
            }

            private void applyResult(String itemKey, boolean success, String errorMessage) {
                if (cancelled.get()) {
                    return;
                }

                TaskBatchItemEntity itemEntity = itemMap.get(itemKey);
                if (itemEntity == null) {
                    return;
                }

                if (success) {
                    itemEntity.setStatus(TaskItemStatus.SUCCESS);
                    successCount.incrementAndGet();
                } else {
                    itemEntity.setStatus(TaskItemStatus.FAILED);
                    itemEntity.setErrorMessage(errorMessage != null ? errorMessage : "Handler returned false");
                    failCount.incrementAndGet();
                }

                dirtyItems.add(itemEntity);

                int completed = completedCounter.incrementAndGet();

                if (completed % STATUS_CHECK_INTERVAL == 0) {
                    if (!taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
                        cancelled.set(true);
                        log.info("Task [{}] no longer running after {} items, stopping", taskId, completed);
                        return;
                    }
                }

                if (flushCounter.incrementAndGet() % progressUpdateInterval == 0 || completed == pendingItems.size()) {
                    flushProgress(taskId, dirtyItems, savedItemIds, successCount, failCount, skippedCount);
                }
            }

            @Override
            public boolean isCancelled() {
                return cancelled.get();
            }

            @Override
            public void flush() {
                flushProgress(taskId, dirtyItems, savedItemIds, successCount, failCount, skippedCount);
                log.info("Task [{}] explicit flush: success={}, fail={}, completed={}/{}",
                        taskId, successCount.get(), failCount.get(),
                        completedCounter.get(), pendingItems.size());
            }
        };

        try {
            log.info("Task [{}] calling handler.processBatch with {} items", taskId, itemKeys.size());
            handler.processBatch(itemKeys, params, callback);
            log.info("Task [{}] handler.processBatch returned, successCount={}, failCount={}",
                    taskId, successCount.get(), failCount.get());
        } catch (Exception e) {
            cancelled.set(true);
            log.error("Task [{}] execution failed in processBatch", taskId, e);
        }

        log.info("Task [{}] flushing final progress...", taskId);
        flushProgress(taskId, dirtyItems, savedItemIds, successCount, failCount, skippedCount);

        log.info("Task [{}] updating final task status (success={}, fail={}, cancelled={})",
                taskId, successCount.get(), failCount.get(), cancelled.get());

        new TransactionTemplate(transactionManager).execute(status -> {
            taskRepository.findById(taskId).ifPresent(finalTask -> {
                finalTask.setSuccessCount(successCount.get());
                finalTask.setFailCount(failCount.get());
                finalTask.setSkippedCount(skippedCount.get());

                boolean shouldComplete = finalTask.getStatus() == TaskStatus.RUNNING && !cancelled.get();

                if (shouldComplete) {
                    finalTask.setStatus(TaskStatus.COMPLETED);
                    finalTask.setEndTime(LocalDateTime.now());
                }
                taskRepository.save(finalTask);

                if (shouldComplete) {
                    handler.afterComplete(params);
                    log.info("Task [{}] completed: success={}, fail={}",
                            taskId, successCount.get(), failCount.get());
                } else {
                    log.info("Task [{}] finished with status={}, success={}, fail={}",
                            taskId, finalTask.getStatus(), successCount.get(), failCount.get());
                }
            });
            // 终态清理：删除已完成的 items（SUCCESS/SKIPPED），保留 FAILED 供重试
            cleanupTerminalItems(taskId);
            return null;
        });
    }

    private void flushProgress(Integer taskId, List<TaskBatchItemEntity> dirtyItems,
                               Set<Integer> savedItemIds,
                               AtomicInteger successCount, AtomicInteger failCount,
                               AtomicInteger skippedCount) {
        List<TaskBatchItemEntity> toSave = new ArrayList<>();
        synchronized (dirtyItems) {
            Iterator<TaskBatchItemEntity> it = dirtyItems.iterator();
            while (it.hasNext()) {
                TaskBatchItemEntity item = it.next();
                if (item.getStatus() != TaskItemStatus.PENDING && savedItemIds.add(item.getId())) {
                    toSave.add(item);
                }
                it.remove();
            }
        }

        if (toSave.isEmpty()) {
            return;
        }

        log.info("flushProgress: taskId={}, saving {} items, success={}, fail={}",
                taskId, toSave.size(), successCount.get(), failCount.get());

        new TransactionTemplate(transactionManager).execute(status -> {
            itemRepository.saveAll(toSave);
            taskRepository.findById(taskId).ifPresent(t -> {
                t.setSuccessCount(successCount.get());
                t.setFailCount(failCount.get());
                t.setSkippedCount(skippedCount.get());
                taskRepository.save(t);
            });
            return null;
        });
    }

    // ==================== Conversion ====================

    private TaskVO toVO(TaskBatchEntity entity) {
        TaskVO vo = taskMapper.toVO(entity);
        enrichVO(vo);
        return vo;
    }

    private void enrichVO(TaskVO vo) {
        vo.setTaskTypeLabel(vo.getTaskType());
        if (vo.getStatus() != null) {
            vo.setStatusLabel(vo.getStatus().getLabel());
        }
        vo.setSuccessCount(vo.getSuccessCount() != null ? vo.getSuccessCount() : 0);
        vo.setFailCount(vo.getFailCount() != null ? vo.getFailCount() : 0);
        vo.setSkippedCount(vo.getSkippedCount() != null ? vo.getSkippedCount() : 0);

        if (vo.getTotalCount() != null && vo.getTotalCount() > 0) {
            int completed = vo.getSuccessCount() + vo.getFailCount() + vo.getSkippedCount();
            vo.setProgress((int) Math.min(100, (completed * 100.0) / vo.getTotalCount()));
        } else {
            vo.setProgress(0);
        }

        if (vo.getStartTime() != null && vo.getStatus() == TaskStatus.RUNNING && vo.getProgress() > 0) {
            long elapsedMs = Duration.between(vo.getStartTime(), LocalDateTime.now()).toMillis();
            long remainingMs = (long) ((elapsedMs / (double) vo.getProgress()) * (100 - vo.getProgress()));
            long remainingSeconds = remainingMs / 1000;
            vo.setEstimatedRemainingSeconds(remainingSeconds);
            vo.setFormattedRemainingTime(formatDuration(remainingSeconds));
        }
    }

    private String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) return "";
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("分");
        }
        sb.append(seconds).append("秒");
        return sb.toString();
    }

    // ==================== Utility ====================

    private String toJson(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize params to JSON", e);
            return null;
        }
    }

    private Map<String, Object> parseParams(String params) {
        if (!StringUtils.hasText(params)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(params, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse params JSON", e);
            return new HashMap<>();
        }
    }
}
