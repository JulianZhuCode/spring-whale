package io.github.springwhale.platform.task.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.task.dao.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.dao.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.dao.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.dao.repository.TaskBatchRepository;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.dto.vo.TaskVO;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.handler.TaskHandler;
import io.github.springwhale.platform.task.mapper.TaskMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core service for batch task management.
 * <p>
 * Provides task lifecycle management: creation, execution (with concurrency),
 * pause, resume (breakpoint), cancel, and progress tracking.
 * Delegates execution to {@link TaskExecutionEngine}.
 * </p>
 */
@Slf4j
public class TaskService {

    private final TaskBatchRepository taskRepository;
    private final TaskBatchItemRepository itemRepository;
    private final TaskMapper taskMapper;
    private final List<TaskHandler> handlers;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutionEngine executionEngine;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .findAndRegisterModules();
    private Map<String, TaskHandler> handlerMap;

    public TaskService(TaskBatchRepository taskRepository,
                       TaskBatchItemRepository itemRepository,
                       TaskMapper taskMapper,
                       List<TaskHandler> handlers,
                       PlatformTransactionManager transactionManager) {
        this.taskRepository = taskRepository;
        this.itemRepository = itemRepository;
        this.taskMapper = taskMapper;
        this.handlers = handlers;
        this.transactionManager = transactionManager;
        this.executionEngine = new TaskExecutionEngine(taskRepository, itemRepository, transactionManager);
    }

    @PostConstruct
    public void init() {
        this.handlerMap = new HashMap<>();
        for (TaskHandler h : handlers) {
            this.handlerMap.put(h.getTaskType(), h);
        }
        this.executionEngine.init();
        log.info("TaskService initialized");
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
        task.setConcurrency(request.getConcurrency());

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

        log.info("Created task [{}] type={}, items={}, concurrency={}",
                task.getId(), request.getTaskType(), itemKeys.size(), task.getConcurrency());
        return toVO(task);
    }

    /**
     * Starts executing a task. If the task is PAUSED or has RUNNING items,
     * it resumes from the breakpoint (skipping already-successful items).
     */
    @Transactional
    public TaskVO start(Long taskId) {
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

        executionEngine.submitAfterCommit(taskId, handler, params);

        return toVO(task);
    }

    /**
     * Pauses a running task. The current in-flight items will complete,
     * but no new items will be picked up.
     */
    @Transactional
    public TaskVO pause(Long taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.RUNNING) {
            throw BusinessException.create("TASK_NOT_RUNNING", "Task is not running: " + taskId);
        }

        task.setStatus(TaskStatus.PAUSED);
        taskRepository.save(task);

        executionEngine.cancelFuture(taskId);

        log.info("Paused task [{}]", taskId);
        return toVO(task);
    }

    /**
     * Resumes a paused task from the breakpoint.
     */
    @Transactional
    public TaskVO resume(Long taskId) {
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

        executionEngine.submitAfterCommit(taskId, handler, params);

        return toVO(task);
    }

    /**
     * Cancels a task. In-flight items may still complete, but no new items are picked up.
     */
    @Transactional
    public TaskVO cancel(Long taskId) {
        TaskBatchEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + taskId));

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            return toVO(task);
        }

        if (task.getStatus() == TaskStatus.RUNNING) {
            executionEngine.cancelFuture(taskId);
        }

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());
        handler.onCancel(params);

        task.setStatus(TaskStatus.CANCELLED);
        task.setEndTime(LocalDateTime.now());
        taskRepository.save(task);

        executionEngine.cleanupTerminalItems(taskId);

        log.info("Cancelled task [{}]", taskId);
        return toVO(task);
    }

    /**
     * Deletes a terminal task (COMPLETED / CANCELLED / FAILED).
     * Running / Paused tasks cannot be deleted; use cancel first.
     */
    @Transactional
    public void delete(Long taskId) {
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

    public Optional<TaskVO> findById(Long id) {
        return taskRepository.findById(id).map(this::toVO);
    }

    /**
     * Returns all failed items for a given task.
     */
    public List<TaskBatchItemEntity> findFailedItems(Long taskId) {
        return itemRepository.findByTaskIdAndStatusOrderByIdAsc(taskId, TaskItemStatus.FAILED);
    }

    /**
     * Resets FAILED items back to PENDING and starts/resumes execution immediately.
     */
    @Transactional
    public TaskVO retryFailed(Long taskId) {
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
        task.setTotalCount(task.getSuccessCount() + resetCount);

        TaskHandler handler = getHandler(task.getTaskType());
        Map<String, Object> params = parseParams(task.getParams());

        if (task.getStartTime() == null) {
            handler.beforeStart(params);
            task.setStartTime(LocalDateTime.now());
        }
        task.setStatus(TaskStatus.RUNNING);
        taskRepository.save(task);

        log.info("Retry: reset {} failed items to PENDING for task [{}], totalCount={}, failCount=0",
                resetCount, taskId, task.getTotalCount());

        executionEngine.submitAfterCommit(taskId, handler, params);
        return toVO(task);
    }

    public Page<TaskVO> findAll(int page, int size, String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return taskRepository.findAll(pageable).map(this::toVO);
    }

    public Page<TaskVO> findByStatus(TaskStatus status, int page, int size, String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return taskRepository.findByStatus(status, pageable).map(this::toVO);
    }

    public Page<TaskVO> findByTaskType(String taskType, int page, int size, String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
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