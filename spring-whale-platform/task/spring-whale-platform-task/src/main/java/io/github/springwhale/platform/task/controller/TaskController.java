package io.github.springwhale.platform.task.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.task.dao.entity.TaskBatchItemEntity;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.dto.vo.TaskVO;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for batch task management.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new batch task.
     * POST /api/tasks
     */
    @PostMapping
    public TaskVO create(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.create(request);
    }

    /**
     * Find task by ID.
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public TaskVO findById(@PathVariable Integer id) {
        return taskService.findById(id)
                .orElseThrow(() -> BusinessException.create("TASK_NOT_FOUND", "Task not found: " + id));
    }

    /**
     * Find all tasks with pagination.
     * GET /api/tasks?page=0&size=20
     */
    @GetMapping
    public Page<TaskVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        return taskService.findAll(page, size, sort);
    }

    /**
     * Find tasks by status.
     * GET /api/tasks/by-status?status=RUNNING
     */
    @GetMapping("/by-status")
    public Page<TaskVO> findByStatus(
            @RequestParam TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return taskService.findByStatus(status, page, size);
    }

    /**
     * Find tasks by type.
     * GET /api/tasks/by-type?type=WORD_AUDIO
     */
    @GetMapping("/by-type")
    public Page<TaskVO> findByType(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return taskService.findByTaskType(type, page, size);
    }

    /**
     * Start executing a task.
     * POST /api/tasks/{id}/start
     */
    @PostMapping("/{id}/start")
    public TaskVO start(@PathVariable Integer id) {
        return taskService.start(id);
    }

    /**
     * Pause a running task.
     * POST /api/tasks/{id}/pause
     */
    @PostMapping("/{id}/pause")
    public TaskVO pause(@PathVariable Integer id) {
        return taskService.pause(id);
    }

    /**
     * Resume a paused task.
     * POST /api/tasks/{id}/resume
     */
    @PostMapping("/{id}/resume")
    public TaskVO resume(@PathVariable Integer id) {
        return taskService.resume(id);
    }

    /**
     * Cancel a task.
     * POST /api/tasks/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public TaskVO cancel(@PathVariable Integer id) {
        return taskService.cancel(id);
    }

    /**
     * Delete a terminal task (COMPLETED / CANCELLED / FAILED).
     * DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        taskService.delete(id);
    }

    /**
     * Get failed items for a task.
     * GET /api/tasks/{id}/failed-items
     */
    @GetMapping("/{id}/failed-items")
    public List<TaskBatchItemEntity> findFailedItems(@PathVariable Integer id) {
        return taskService.findFailedItems(id);
    }

    /**
     * Retry failed items. Resets FAILED items to PENDING and task to PAUSED.
     * POST /api/tasks/{id}/retry-failed
     */
    @PostMapping("/{id}/retry-failed")
    public TaskVO retryFailed(@PathVariable Integer id) {
        return taskService.retryFailed(id);
    }
}
