package io.github.springwhale.task.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.task.dto.request.TaskCreateRequest;
import io.github.springwhale.task.dto.vo.TaskVO;
import io.github.springwhale.task.entity.TaskBatchEntity;
import io.github.springwhale.task.entity.TaskBatchItemEntity;
import io.github.springwhale.task.enums.TaskItemStatus;
import io.github.springwhale.task.enums.TaskStatus;
import io.github.springwhale.task.handler.TaskHandler;
import io.github.springwhale.task.mapper.TaskMapper;
import io.github.springwhale.task.repository.TaskBatchItemRepository;
import io.github.springwhale.task.repository.TaskBatchRepository;
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
    private static final int ITEM_CONCURRENCY = Runtime.getRuntime().availableProcessors() * 2;
    private static final int STATUS_CHECK_INTERVAL = 100;
    private static final int PROGRESS_UPDATE_INTERVAL = 100;
    private final TaskBatchRepository taskRepository;
    private final TaskBatchItemRepository itemRepository;
    private final TaskMapper taskMapper;
    private final List<TaskHandler> handlers;
    private final PlatformTransactionManager transactionManager;

    private ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .findAndRegisterModules();
    private final Map<String, Future<?>> runningFutures = new ConcurrentHashMap<>();
    private Map<String, TaskHandler> handlerMap;
    private ExecutorService taskExecutor;
    private ExecutorService itemExecutor;

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
        this.itemExecutor = new ThreadPoolExecutor(
                ITEM_CONCURRENCY,
                ITEM_CONCURRENCY,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r, "item-executor");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("TaskService initialized: {} task threads, {} item threads",
                DEFAULT_CONCURRENCY, ITEM_CONCURRENCY);
    }

    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdownNow();
        }
        if (itemExecutor != null) {
            itemExecutor.shutdownNow();
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
     */
    @Transactional
    public TaskVO create(TaskCreateRequest request) {
        TaskHandler handler = getHandler(request.getTaskType());

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
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() == TaskStatus.RUNNING) {
            throw BusinessException.create("TASK_ALREADY_RUNNING", "Task is already running: " + taskId);
        }
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw BusinessException.create("TASK_FINISHED", "Task is already finished: " + taskId);
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        handler.beforeStart(params);

        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(task.getStartTime() != null ? task.getStartTime() : LocalDateTime.now());
        taskRepository.save(task);

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
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.PAUSED) {
            throw BusinessException.create("TASK_NOT_PAUSED", "Task is not paused: " + taskId);
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        task.setStatus(TaskStatus.RUNNING);
        taskRepository.save(task);

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

        log.info("Cancelled task [{}]", taskId);
        return toVO(task);
    }

    // ==================== Query Methods ====================

    public Optional<TaskVO> findById(Integer id) {
        return taskRepository.findById(id).map(this::toVO);
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
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    TaskBatchEntity freshTask = taskRepository.findById(taskId).orElse(null);
                    if (freshTask != null && freshTask.getStatus() == TaskStatus.RUNNING) {
                        submitForExecution(freshTask, handler, params);
                    }
                }
            });
        } else {
            TaskBatchEntity freshTask = taskRepository.findById(taskId).orElse(null);
            if (freshTask != null && freshTask.getStatus() == TaskStatus.RUNNING) {
                submitForExecution(freshTask, handler, params);
            }
        }
    }

    private void submitForExecution(TaskBatchEntity task, TaskHandler handler, Map<String, Object> params) {
        Future<?> future = taskExecutor.submit(() -> {
            try {
                executeTask(task, handler, params);
            } catch (Exception e) {
                log.error("Task [{}] execution failed with unexpected error", task.getId(), e);
                new TransactionTemplate(transactionManager).execute(status -> {
                    taskRepository.findById(task.getId()).ifPresent(t -> {
                        t.setStatus(TaskStatus.FAILED);
                        t.setErrorMessage(e.getMessage());
                        t.setEndTime(LocalDateTime.now());
                        taskRepository.save(t);
                    });
                    return null;
                });
            } finally {
                runningFutures.remove(String.valueOf(task.getId()));
            }
        });
        runningFutures.put(String.valueOf(task.getId()), future);
    }

    private void executeTask(TaskBatchEntity task, TaskHandler handler, Map<String, Object> params) {
        Integer taskId = task.getId();

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
        AtomicInteger submittedCounter = new AtomicInteger(0);
        AtomicInteger completedCounter = new AtomicInteger(0);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        log.info("Task [{}]: starting {} pending items (total={}, success={}, fail={})",
                taskId, pendingItems.size(), total, successCount.get(), failCount.get());

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        List<CompletableFuture<Void>> futures = new ArrayList<>(pendingItems.size());

        for (TaskBatchItemEntity item : pendingItems) {
            if (Thread.currentThread().isInterrupted()) {
                cancelled.set(true);
                log.info("Task [{}] submission thread interrupted, stopping", taskId);
                break;
            }

            int submitted = submittedCounter.incrementAndGet();
            if (submitted > 0 && submitted % STATUS_CHECK_INTERVAL == 0) {
                if (!taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
                    cancelled.set(true);
                    log.info("Task [{}] no longer running after {} submissions, stopping", taskId, submitted);
                    break;
                }
            }

            if (cancelled.get()) {
                break;
            }

            final Integer itemId = item.getId();
            final String itemKey = item.getItemKey();

            CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
                if (cancelled.get()) {
                    return;
                }

                TaskBatchItemEntity itemEntity = itemRepository.findById(itemId).orElse(null);
                if (itemEntity == null) {
                    return;
                }

                try {
                    boolean itemSuccess = handler.processItem(itemKey, params);
                    if (itemSuccess) {
                        itemEntity.setStatus(TaskItemStatus.SUCCESS);
                        successCount.incrementAndGet();
                    } else {
                        itemEntity.setStatus(TaskItemStatus.FAILED);
                        itemEntity.setErrorMessage("Handler returned false");
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("Task [{}] item [{}] failed", taskId, itemKey, e);
                    itemEntity.setStatus(TaskItemStatus.FAILED);
                    itemEntity.setErrorMessage(e.getMessage());
                    itemEntity.setRetryCount((itemEntity.getRetryCount() != null ? itemEntity.getRetryCount() : 0) + 1);
                    failCount.incrementAndGet();
                }

                int completed = completedCounter.incrementAndGet();
                if (completed % PROGRESS_UPDATE_INTERVAL == 0) {
                    txTemplate.execute(status -> {
                        itemRepository.save(itemEntity);
                        taskRepository.findById(taskId).ifPresent(t -> {
                            t.setSuccessCount(successCount.get());
                            t.setFailCount(failCount.get());
                            t.setSkippedCount(skippedCount.get());
                            taskRepository.save(t);
                        });
                        return null;
                    });
                } else {
                    txTemplate.execute(status -> {
                        itemRepository.save(itemEntity);
                        return null;
                    });
                }
            }, itemExecutor);

            futures.add(cf);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            cancelled.set(true);
            log.warn("Task [{}] execution interrupted or failed: {}", taskId, e.getMessage());
        }

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

                if (shouldComplete && TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                handler.afterComplete(params);
                                log.info("Task [{}] completed: success={}, fail={}",
                                        taskId, successCount.get(), failCount.get());
                            } catch (Exception e) {
                                log.error("Task [{}] afterComplete callback failed", taskId, e);
                            }
                        }
                    });
                } else {
                    log.info("Task [{}] finished with status={}, success={}, fail={}",
                            taskId, finalTask.getStatus(), successCount.get(), failCount.get());
                }
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
            vo.setEstimatedRemainingSeconds(remainingMs / 1000);
        }
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
