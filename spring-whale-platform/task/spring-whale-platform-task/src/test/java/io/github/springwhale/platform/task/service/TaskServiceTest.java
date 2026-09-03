package io.github.springwhale.platform.task.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.task.TestSecurityConfiguration;
import io.github.springwhale.platform.task.dao.entity.TaskBatchEntity;
import io.github.springwhale.platform.task.dao.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.dao.repository.TaskBatchRepository;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.dto.vo.TaskVO;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.support.TaskTestConfiguration;
import io.github.springwhale.platform.task.support.TestTaskHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestSecurityConfiguration.class, TaskTestConfiguration.class})
@DisplayName("TaskService Tests")
class TaskServiceTest {

    private static final long TIMEOUT_MS = 30_000;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskBatchRepository taskRepository;

    @Autowired
    private TaskBatchItemRepository itemRepository;

    @Autowired
    private TestTaskHandler testTaskHandler;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        taskRepository.deleteAll();
        testTaskHandler.reset();
    }

    // ==================== Recovery ====================

    @Test
    @DisplayName("interrupted RUNNING tasks are recovered to PAUSED")
    void recoverInterruptedTasks() {
        TaskBatchEntity interrupted = new TaskBatchEntity();
        interrupted.setTaskType(TestTaskHandler.TASK_TYPE);
        interrupted.setStatus(TaskStatus.RUNNING);
        interrupted.setTotalCount(10);
        taskRepository.save(interrupted);

        TaskBatchEntity finished = new TaskBatchEntity();
        finished.setTaskType(TestTaskHandler.TASK_TYPE);
        finished.setStatus(TaskStatus.COMPLETED);
        finished.setTotalCount(0);
        taskRepository.save(finished);

        int recovered = taskService.recoverInterruptedTasks();

        assertEquals(1, recovered);
        assertEquals(TaskStatus.PAUSED,
                taskRepository.findById(interrupted.getId()).orElseThrow().getStatus());
        assertEquals(TaskStatus.COMPLETED,
                taskRepository.findById(finished.getId()).orElseThrow().getStatus());
    }

    // ==================== Creation ====================

    @Test
    @DisplayName("creating a task enumerates and persists all items")
    void createPersistsItems() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType(TestTaskHandler.TASK_TYPE);
        request.setParams(Map.of("count", 7));

        TaskVO vo = taskService.create(request);

        assertEquals(TaskStatus.PENDING, vo.getStatus());
        assertEquals(7, vo.getTotalCount());
        assertEquals(7, itemRepository.countByTaskId(vo.getId()));
    }

    @Test
    @DisplayName("a second active task of the same type returns the existing one")
    void createIsExclusivePerType() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType(TestTaskHandler.TASK_TYPE);
        request.setParams(Map.of("count", 3));

        TaskVO first = taskService.create(request);
        TaskVO second = taskService.create(request);

        assertEquals(first.getId(), second.getId());
        assertEquals(1, taskRepository.count());
    }

    @Test
    @DisplayName("unknown task type raises TASK_TYPE_NOT_FOUND")
    void createUnknownType() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType("NO_SUCH_TYPE");

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.create(request));
        assertEquals("TASK_TYPE_NOT_FOUND", ex.getErrorCode());
    }

    // ==================== Lifecycle errors ====================

    @Test
    @DisplayName("starting a non-existent task raises TASK_NOT_FOUND")
    void startNotFound() {
        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.start(99999L));
        assertEquals("TASK_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("pausing a PENDING task raises TASK_NOT_RUNNING")
    void pausePending() {
        Long id = createTask(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.pause(id));
        assertEquals("TASK_NOT_RUNNING", ex.getErrorCode());
    }

    @Test
    @DisplayName("resuming a PENDING task raises TASK_NOT_PAUSED")
    void resumePending() {
        Long id = createTask(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.resume(id));
        assertEquals("TASK_NOT_PAUSED", ex.getErrorCode());
    }

    @Test
    @DisplayName("deleting a non-existent task raises TASK_NOT_FOUND")
    void deleteNotFound() {
        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.delete(99999L));
        assertEquals("TASK_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    @DisplayName("deleting an active task raises TASK_ACTIVE")
    void deleteActive() {
        Long id = createTask(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.delete(id));
        assertEquals("TASK_ACTIVE", ex.getErrorCode());
    }

    @Test
    @DisplayName("retrying an active task raises TASK_ACTIVE")
    void retryFailedWhileActive() {
        Long id = createTask(2);

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.retryFailed(id));
        assertEquals("TASK_ACTIVE", ex.getErrorCode());
    }

    // ==================== Full happy path through the engine ====================

    @Test
    @DisplayName("start runs items asynchronously to COMPLETED, then retry raises NO_FAILED_ITEMS")
    void startThenRetryWithoutFailures() {
        Long id = createTask(4);

        taskService.start(id);
        TaskVO completed = awaitStatus(id, TaskStatus.COMPLETED);
        assertEquals(4, completed.getSuccessCount());
        assertEquals(0, completed.getFailCount());
        assertEquals(1, testTaskHandler.afterCompleteCount.get());

        BusinessException ex = assertThrows(BusinessException.class, () -> taskService.retryFailed(id));
        assertEquals("NO_FAILED_ITEMS", ex.getErrorCode());
    }

    @Test
    @DisplayName("a fatal batch error marks the task FAILED instead of hanging in RUNNING")
    void fatalBatchFailureMarksFailed() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType(TestTaskHandler.TASK_TYPE);
        request.setParams(Map.of("count", 3, "fatalBatch", true));
        Long id = taskService.create(request).getId();

        taskService.start(id);

        TaskVO failed = awaitStatus(id, TaskStatus.FAILED);
        assertNotNull(failed.getErrorMessage());
        assertTrue(failed.getErrorMessage().contains("simulated fatal batch failure"));
        assertNotNull(failed.getEndTime());
        // afterComplete must not be invoked on failure.
        assertEquals(0, testTaskHandler.afterCompleteCount.get());

        // FAILED is terminal: startup recovery leaves it alone, and it can be deleted.
        assertEquals(0, taskService.recoverInterruptedTasks());
        taskService.delete(id);
        assertTrue(taskService.findById(id).isEmpty());
    }

    @Test
    @DisplayName("cancelling a PENDING task invokes onCancel and ends CANCELLED")
    void cancelPending() {
        Long id = createTask(2);

        TaskVO vo = taskService.cancel(id);

        assertEquals(TaskStatus.CANCELLED, vo.getStatus());
        assertEquals(1, testTaskHandler.onCancelCount.get());
        // Terminal task can then be deleted.
        taskService.delete(id);
        assertTrue(taskService.findById(id).isEmpty());
    }

    // ==================== Helpers ====================

    private Long createTask(int count) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType(TestTaskHandler.TASK_TYPE);
        request.setParams(Map.of("count", count));
        return taskService.create(request).getId();
    }

    private TaskVO awaitStatus(Long taskId, TaskStatus expected) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            TaskVO vo = taskService.findById(taskId).orElseThrow();
            if (vo.getStatus() == expected) {
                return vo;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Task [" + taskId + "] did not reach status " + expected);
    }
}
