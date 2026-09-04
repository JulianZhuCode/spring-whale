package io.github.springwhale.platform.task.service;

import io.github.springwhale.platform.task.dao.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.dao.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.dao.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.dao.repository.TaskBatchRepository;
import io.github.springwhale.platform.task.enums.TaskItemStatus;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.handler.TaskHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Execution engine for batch task processing.
 * <p>
 * Handles asynchronous task execution with paged loading, progress tracking,
 * and optional concurrency via virtual threads.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionEngine {

    private static final int BATCH_LOAD_SIZE = 500;
    private static final int DEFAULT_CONCURRENCY = 4;
    private static final int STATUS_CHECK_INTERVAL = 100;

    private final TaskBatchRepository taskRepository;
    private final TaskBatchItemRepository itemRepository;
    private final PlatformTransactionManager transactionManager;

    private final Map<String, Future<?>> runningFutures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicLong> executionEpochs = new ConcurrentHashMap<>();
    private final ReentrantLock flushLock = new ReentrantLock();
    private ExecutorService taskExecutor;

    @PostConstruct
    public void init() {
        this.taskExecutor = Executors.newVirtualThreadPerTaskExecutor();
        log.info("TaskExecutionEngine initialized with virtual threads");
    }

    @PreDestroy
    public void destroy() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
        }
    }

    public void cancelFuture(Long taskId) {
        Future<?> future = runningFutures.get(String.valueOf(taskId));
        if (future != null) {
            future.cancel(true);
        }
    }

    public void cancelAndAwait(Long taskId, long timeoutMs) {
        Future<?> future = runningFutures.get(String.valueOf(taskId));
        if (future != null) {
            future.cancel(true);
            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("Task [{}] future did not complete within {}ms after cancel", taskId, timeoutMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for task [{}] future", taskId);
            } catch (ExecutionException e) {
                log.debug("Task [{}] future completed with exception after cancel: {}", taskId, e.getMessage());
            } catch (CancellationException e) {
                log.debug("Task [{}] future was cancelled", taskId);
            }
        }
    }

    public void awaitFutureDone(Long taskId, long timeoutMs) {
        Future<?> future = runningFutures.get(String.valueOf(taskId));
        if (future != null) {
            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("Task [{}] future did not complete within {}ms", taskId, timeoutMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while waiting for task [{}] future", taskId);
            } catch (ExecutionException | CancellationException e) {
                log.debug("Task [{}] future completed: {}", taskId, e.getMessage());
            }
        }
    }

    public long incrementEpoch(Long taskId) {
        AtomicLong epoch = executionEpochs.computeIfAbsent(taskId, k -> new AtomicLong(0));
        long newEpoch = epoch.incrementAndGet();
        log.info("Task [{}] execution epoch incremented to {}", taskId, newEpoch);
        return newEpoch;
    }

    public long getEpoch(Long taskId) {
        AtomicLong epoch = executionEpochs.get(taskId);
        return epoch != null ? epoch.get() : 0;
    }

    private boolean isCurrentExecutor(Long taskId, long myEpoch) {
        AtomicLong current = executionEpochs.get(taskId);
        return current != null && current.get() == myEpoch;
    }

    public void cleanupTerminalItems(Long taskId) {
        itemRepository.deleteByTaskIdAndStatusIn(taskId,
                List.of(TaskItemStatus.SUCCESS, TaskItemStatus.SKIPPED));
        log.info("Cleaned up SUCCESS/SKIPPED items for task [{}]", taskId);
    }

    public void submitAfterCommit(Long taskId, TaskHandler handler, Map<String, Object> params) {
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
        Long taskId = task.getId();
        long myEpoch = getEpoch(taskId);
        log.info("submitForExecution() for taskId={}, handler={}, epoch={}, thread={}",
                taskId, handler.getTaskType(), myEpoch, Thread.currentThread().getName());
        AtomicReference<Future<?>> futureRef = new AtomicReference<>();
        Future<?> future = taskExecutor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            log.info("Task [{}] execution started on thread={}, epoch={}", taskId, threadName, myEpoch);
            try {
                executeTask(task, handler, params, myEpoch);
                log.info("Task [{}] execution completed on thread={}", taskId, threadName);
            } catch (Exception e) {
                log.error("Task [{}] execution failed with unexpected error", task.getId(), e);
                new TransactionTemplate(transactionManager).execute(status -> {
                    taskRepository.findById(task.getId()).ifPresent(t -> {
                        if (t.getStatus() == TaskStatus.RUNNING && isCurrentExecutor(taskId, myEpoch)) {
                            t.setStatus(TaskStatus.FAILED);
                            t.setErrorMessage(e.getMessage());
                            t.setEndTime(LocalDateTime.now());
                            taskRepository.save(t);
                            log.warn("Task [{}] status set to FAILED due to exception: {}", task.getId(), e.getMessage());
                        } else {
                            log.info("Task [{}] status is {} (not RUNNING) or epoch changed, skipping FAILED transition",
                                    task.getId(), t.getStatus());
                        }
                    });
                    cleanupTerminalItems(task.getId());
                    return null;
                });
            } finally {
                runningFutures.remove(String.valueOf(taskId), futureRef.get());
                log.info("Task [{}] future removed from runningFutures (epoch={})", taskId, myEpoch);
            }
        });
        futureRef.set(future);
        runningFutures.put(String.valueOf(taskId), future);
        log.info("Task [{}] submitted to thread pool, future={}, epoch={}", taskId, future, myEpoch);
    }

    private void executeTask(TaskBatchEntity task, TaskHandler handler, Map<String, Object> params, long myEpoch) {
        Long taskId = task.getId();
        int total = task.getTotalCount();
        int concurrency = task.getConcurrency() != null && task.getConcurrency() > 0
                ? task.getConcurrency() : DEFAULT_CONCURRENCY;

        log.info("executeTask() started for taskId={}, total={}, concurrency={}, epoch={}, thread={}",
                taskId, total, concurrency, myEpoch, Thread.currentThread().getName());

        if (!isCurrentExecutor(taskId, myEpoch) || !taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
            log.info("Task [{}] is not RUNNING or epoch changed, skipping execution", taskId);
            return;
        }

        AtomicInteger successCount = new AtomicInteger(task.getSuccessCount() != null ? task.getSuccessCount() : 0);
        AtomicInteger failCount = new AtomicInteger(task.getFailCount() != null ? task.getFailCount() : 0);
        AtomicInteger skippedCount = new AtomicInteger(task.getSkippedCount() != null ? task.getSkippedCount() : 0);
        AtomicInteger completedCounter = new AtomicInteger(0);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        // A fatal error thrown by the handler's batch processing (not per-item failures,
        // which are reported via the callback). The task must reach a terminal FAILED state
        // instead of hanging in RUNNING.
        AtomicReference<Throwable> batchFailure = new AtomicReference<>();

        Set<Long> savedItemIds = ConcurrentHashMap.newKeySet();
        List<TaskBatchItemEntity> dirtyItems = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger flushCounter = new AtomicInteger(0);

        int progressUpdateInterval = Math.max(20, Math.min(100, total / 50));

        int offset = 0;
        while (true) {
            if (cancelled.get()) {
                break;
            }

            if (!isCurrentExecutor(taskId, myEpoch) || !taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
                cancelled.set(true);
                log.info("Task [{}] no longer running or epoch changed at offset {}, stopping", taskId, offset);
                break;
            }

            Pageable pageable = PageRequest.of(0, BATCH_LOAD_SIZE,
                    Sort.by(Sort.Direction.ASC, "id"));
            List<TaskBatchItemEntity> batch = itemRepository
                    .findByTaskIdAndStatus(taskId, TaskItemStatus.PENDING, pageable);

            if (batch.isEmpty()) {
                break;
            }

            log.info("Task [{}]: loaded batch of {} items at offset {} (total={}, success={}, fail={})",
                    taskId, batch.size(), offset, total, successCount.get(), failCount.get());

            Map<String, TaskBatchItemEntity> itemMap = batch.stream()
                    .collect(Collectors.toMap(TaskBatchItemEntity::getItemKey, Function.identity()));
            List<String> itemKeys = batch.stream()
                    .map(TaskBatchItemEntity::getItemKey)
                    .collect(Collectors.toList());

            int batchSize = batch.size();

            TaskHandler.BatchProgressCallback callback = createCallback(taskId, itemMap, batchSize,
                    successCount, failCount, skippedCount, completedCounter, cancelled,
                    dirtyItems, savedItemIds, flushCounter, progressUpdateInterval, myEpoch);

            try {
                if (concurrency > 1 && batchSize > concurrency) {
                    processBatchConcurrently(itemKeys, handler, params, callback, concurrency);
                } else {
                    handler.processBatch(itemKeys, params, callback);
                }
            } catch (Exception e) {
                cancelled.set(true);
                batchFailure.compareAndSet(null, e);
                log.error("Task [{}] execution failed in processBatch at offset {}", taskId, offset, e);
            }

            offset += batch.size();
        }

        log.info("Task [{}] flushing final progress...", taskId);
        flushProgress(taskId, dirtyItems, savedItemIds, successCount, failCount, skippedCount);

        log.info("Task [{}] updating final task status (success={}, fail={}, cancelled={})",
                taskId, successCount.get(), failCount.get(), cancelled.get());

        Throwable fatalError = batchFailure.get();
        new TransactionTemplate(transactionManager).execute(status -> {
            taskRepository.findById(taskId).ifPresent(finalTask -> {
                finalTask.setSuccessCount(successCount.get());
                finalTask.setFailCount(failCount.get());
                finalTask.setSkippedCount(skippedCount.get());

                boolean stillRunning = finalTask.getStatus() == TaskStatus.RUNNING;
                boolean shouldFail = stillRunning && fatalError != null;
                boolean shouldComplete = stillRunning && !cancelled.get();

                if (shouldFail) {
                    finalTask.setStatus(TaskStatus.FAILED);
                    finalTask.setErrorMessage(fatalError.getMessage());
                    finalTask.setEndTime(LocalDateTime.now());
                    log.warn("Task [{}] marked FAILED due to fatal batch error: {}",
                            taskId, fatalError.getMessage());
                } else if (shouldComplete) {
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
            cleanupTerminalItems(taskId);
            return null;
        });
    }

    private void processBatchConcurrently(List<String> itemKeys, TaskHandler handler,
                                          Map<String, Object> params,
                                          TaskHandler.BatchProgressCallback callback,
                                          int concurrency) {
        int chunkSize = Math.max(1, itemKeys.size() / concurrency);
        List<List<String>> chunks = partition(itemKeys, chunkSize);

        try (ExecutorService chunkExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = chunks.stream()
                    .<Future<?>>map(chunk -> chunkExecutor.submit(() -> {
                        try {
                            handler.processBatch(chunk, params, callback);
                        } catch (Exception e) {
                            log.error("Chunk processing failed", e);
                        }
                    }))
                    .toList();

            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException e) {
                    log.error("Chunk execution failed", e.getCause());
                }
            }
        }
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }

    private TaskHandler.BatchProgressCallback createCallback(
            Long taskId,
            Map<String, TaskBatchItemEntity> itemMap,
            int batchSize,
            AtomicInteger successCount,
            AtomicInteger failCount,
            AtomicInteger skippedCount,
            AtomicInteger completedCounter,
            AtomicBoolean cancelled,
            List<TaskBatchItemEntity> dirtyItems,
            Set<Long> savedItemIds,
            AtomicInteger flushCounter,
            int progressUpdateInterval,
            long myEpoch) {

        return new TaskHandler.BatchProgressCallback() {
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
                    if (!isCurrentExecutor(taskId, myEpoch) || !taskRepository.existsByIdAndStatus(taskId, TaskStatus.RUNNING)) {
                        cancelled.set(true);
                        log.info("Task [{}] no longer running or epoch changed after {} items, stopping", taskId, completed);
                        return;
                    }
                }

                if (flushCounter.incrementAndGet() % progressUpdateInterval == 0 || completed % batchSize == 0) {
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
                log.info("Task [{}] explicit flush: success={}, fail={}, completed={}",
                        taskId, successCount.get(), failCount.get(), completedCounter.get());
            }
        };
    }

    private void flushProgress(Long taskId, List<TaskBatchItemEntity> dirtyItems,
                               Set<Long> savedItemIds,
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

        flushLock.lock();
        try {
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
        } finally {
            flushLock.unlock();
        }
    }
}