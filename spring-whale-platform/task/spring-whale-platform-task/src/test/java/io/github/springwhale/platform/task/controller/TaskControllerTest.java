package io.github.springwhale.platform.task.controller;

import io.github.springwhale.platform.task.TestSecurityConfiguration;
import io.github.springwhale.platform.task.dao.repository.TaskBatchItemRepository;
import io.github.springwhale.platform.task.dao.repository.TaskBatchRepository;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.support.BlockingTaskHandler;
import io.github.springwhale.platform.task.support.TaskTestConfiguration;
import io.github.springwhale.platform.task.support.TestTaskHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({TestSecurityConfiguration.class, TaskTestConfiguration.class})
@WithMockUser(authorities = {"task:batch", "task:batch:delete"})
@DisplayName("TaskController Integration Tests")
class TaskControllerTest {

    private static final long TIMEOUT_MS = 30_000;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskBatchRepository taskRepository;

    @Autowired
    private TaskBatchItemRepository itemRepository;

    @Autowired
    private TestTaskHandler testTaskHandler;

    @Autowired
    private BlockingTaskHandler blockingTaskHandler;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        taskRepository.deleteAll();
        testTaskHandler.reset();
        // Release any worker threads still blocked from a previous test before resetting.
        blockingTaskHandler.openGate();
        blockingTaskHandler.reset();
    }

    @AfterEach
    void tearDown() {
        blockingTaskHandler.openGate();
    }

    // ==================== Creation ====================

    @Test
    @DisplayName("create task returns PENDING with total count")
    void createTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(TestTaskHandler.TASK_TYPE, Map.of("count", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalCount").value(5))
                .andExpect(jsonPath("$.data.progress").value(0));
    }

    @Test
    @DisplayName("creating a second active task of the same type returns the existing one")
    void createDuplicateReturnsExisting() throws Exception {
        JsonNode first = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 3));
        JsonNode second = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 3));

        assertEquals(first.get("id").asLong(), second.get("id").asLong());
    }

    @Test
    @DisplayName("creating a task without a registered handler returns business error")
    void createUnknownType() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("NO_SUCH_TYPE", Map.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_TYPE_NOT_FOUND"));
    }

    @Test
    @DisplayName("task type is required")
    void createBlankType() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists());
    }

    // ==================== Execution ====================

    @Test
    @DisplayName("all items succeed: task completes with full progress and items are cleaned up")
    void startAllSuccess() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 5)).get("id").asLong();

        startTask(taskId);
        JsonNode task = awaitStatus(taskId, TaskStatus.COMPLETED);

        assertEquals(5, task.get("successCount").asInt());
        assertEquals(0, task.get("failCount").asInt());
        assertEquals(100, task.get("progress").asInt());
        assertEquals(1, testTaskHandler.afterCompleteCount.get());
        // SUCCESS items are cleaned up after completion.
        assertEquals(0, itemRepository.countByTaskId(taskId));
    }

    @Test
    @DisplayName("items that throw are marked FAILED and retained")
    void startWithErrorItems() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE,
                Map.of("count", 3, "errorKeys", List.of("item-2"))).get("id").asLong();

        startTask(taskId);
        JsonNode task = awaitStatus(taskId, TaskStatus.COMPLETED);

        assertEquals(2, task.get("successCount").asInt());
        assertEquals(1, task.get("failCount").asInt());

        mockMvc.perform(get("/api/tasks/{id}/failed-items", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].itemKey").value("item-2"));
    }

    @Test
    @DisplayName("flaky items fail first, then retry-failed resets and processes them to success")
    void retryFailedFlakyItems() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE,
                Map.of("count", 5, "flakyKeys", List.of("item-3", "item-4"))).get("id").asLong();

        startTask(taskId);
        JsonNode firstRun = awaitStatus(taskId, TaskStatus.COMPLETED);
        assertEquals(3, firstRun.get("successCount").asInt());
        assertEquals(2, firstRun.get("failCount").asInt());
        assertEquals(1, testTaskHandler.attemptsOf("item-3"));

        mockMvc.perform(get("/api/tasks/{id}/failed-items", taskId))
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(post("/api/tasks/{id}/retry-failed", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));

        JsonNode retried = awaitStatus(taskId, TaskStatus.COMPLETED);
        assertEquals(5, retried.get("successCount").asInt());
        assertEquals(0, retried.get("failCount").asInt());
        assertEquals(2, testTaskHandler.attemptsOf("item-3"));

        mockMvc.perform(get("/api/tasks/{id}/failed-items", taskId))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("retry-failed on a task without failed items returns business error")
    void retryFailedWithoutFailures() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();
        startTask(taskId);
        awaitStatus(taskId, TaskStatus.COMPLETED);

        mockMvc.perform(post("/api/tasks/{id}/retry-failed", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("NO_FAILED_ITEMS"));
    }

    // ==================== Pause / Resume / Cancel ====================

    @Test
    @DisplayName("pausing a running task and resuming it completes the remaining work")
    void pauseAndResume() throws Exception {
        Long taskId = createTask(BlockingTaskHandler.TASK_TYPE, Map.of("count", 3)).get("id").asLong();

        startTask(taskId);
        awaitEntered(1);

        mockMvc.perform(post("/api/tasks/{id}/pause", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        // Release the blocked items and wait for the paused run to settle
        // (SUCCESS items are cleaned up once the worker notices the PAUSED status).
        blockingTaskHandler.openGate();
        awaitItemCount(taskId, 0);

        mockMvc.perform(post("/api/tasks/{id}/resume", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));

        JsonNode task = awaitStatus(taskId, TaskStatus.COMPLETED);
        assertEquals(3, task.get("successCount").asInt());
        assertEquals(3, blockingTaskHandler.processedCount());
    }

    @Test
    @DisplayName("cancelling a running task invokes the cancel hook and ends CANCELLED")
    void cancelRunning() throws Exception {
        Long taskId = createTask(BlockingTaskHandler.TASK_TYPE, Map.of("count", 3)).get("id").asLong();

        startTask(taskId);
        awaitEntered(1);

        mockMvc.perform(post("/api/tasks/{id}/cancel", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertEquals(1, blockingTaskHandler.onCancelCount());
        awaitStatus(taskId, TaskStatus.CANCELLED);

        // Let the cancelled worker finish so its flushes do not leak into the next test.
        blockingTaskHandler.openGate();
        awaitItemCount(taskId, 0);
    }

    @Test
    @DisplayName("pausing a non-running task returns business error")
    void pauseNonRunning() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();

        mockMvc.perform(post("/api/tasks/{id}/pause", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_NOT_RUNNING"));
    }

    @Test
    @DisplayName("resuming a non-paused task returns business error")
    void resumeNonPaused() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();

        mockMvc.perform(post("/api/tasks/{id}/resume", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_NOT_PAUSED"));
    }

    // ==================== Terminal states / queries ====================

    @Test
    @DisplayName("starting an already finished task returns business error")
    void startFinishedTask() throws Exception {
        Long taskId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();
        startTask(taskId);
        awaitStatus(taskId, TaskStatus.COMPLETED);

        mockMvc.perform(post("/api/tasks/{id}/start", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_FINISHED"));
    }

    @Test
    @DisplayName("getting a non-existent task returns business error")
    void findByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }

    @Test
    @DisplayName("deleting a terminal task removes it; deleting an active one is rejected")
    void deleteTask() throws Exception {
        Long completedId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();
        startTask(completedId);
        awaitStatus(completedId, TaskStatus.COMPLETED);

        mockMvc.perform(delete("/api/tasks/{id}", completedId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tasks/{id}", completedId))
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));

        Long activeId = createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2)).get("id").asLong();
        mockMvc.perform(delete("/api/tasks/{id}", activeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK_ACTIVE"));
    }

    @Test
    @DisplayName("listing by type and by status filters correctly")
    void listFilters() throws Exception {
        createTask(TestTaskHandler.TASK_TYPE, Map.of("count", 2));
        createTask(BlockingTaskHandler.TASK_TYPE, Map.of("count", 2));

        mockMvc.perform(get("/api/tasks/by-type")
                        .param("type", BlockingTaskHandler.TASK_TYPE)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].taskType").value(BlockingTaskHandler.TASK_TYPE));

        mockMvc.perform(get("/api/tasks/by-status")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    // ==================== Helpers ====================

    private String createBody(String taskType, Map<String, Object> params) throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType(taskType);
        request.setParams(params);
        return objectMapper.writeValueAsString(request);
    }

    private JsonNode createTask(String taskType, Map<String, Object> params) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(taskType, params)))
                .andExpect(status().isOk())
                .andReturn();
        return dataOf(result);
    }

    private void startTask(Long taskId) throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/start", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    private JsonNode awaitStatus(Long taskId, TaskStatus expected) throws Exception {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        JsonNode data = null;
        while (System.currentTimeMillis() < deadline) {
            MvcResult result = mockMvc.perform(get("/api/tasks/{id}", taskId))
                    .andExpect(status().isOk())
                    .andReturn();
            data = dataOf(result);
            if (expected.name().equals(data.get("status").asText())) {
                return data;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Task [" + taskId + "] did not reach status " + expected
                + ", last data: " + (data == null ? "null" : data));
    }

    private void awaitEntered(int minimum) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (blockingTaskHandler.enteredCount() >= minimum) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Blocking handler did not enter processing within timeout");
    }

    private void awaitItemCount(Long taskId, long expected) throws Exception {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (itemRepository.countByTaskId(taskId) == expected) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Task [" + taskId + "] item count did not reach " + expected);
    }

    private JsonNode dataOf(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode data = root.get("data");
        assertTrue(data != null && !data.isNull(), "Expected $.data in response: "
                + result.getResponse().getContentAsString());
        return data;
    }
}