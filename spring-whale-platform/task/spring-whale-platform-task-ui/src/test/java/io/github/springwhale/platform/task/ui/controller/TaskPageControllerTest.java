package io.github.springwhale.platform.task.ui.controller;

import io.github.springwhale.platform.task.ui.TestSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@WithMockUser(authorities = {"task:batch", "task:batch:delete"})
@DisplayName("TaskPageController Integration Tests")
class TaskPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("batch page renders with default params")
    void batchPageRenders() throws Exception {
        mockMvc.perform(get("/admin/task"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attributeExists("page"));
    }

    @Test
    @DisplayName("batch page renders via /admin/task/ path")
    void batchPageSlashRenders() throws Exception {
        mockMvc.perform(get("/admin/task/"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"));
    }

    @Test
    @DisplayName("batch page renders via /admin/task/batch path")
    void batchPageFullPathRenders() throws Exception {
        mockMvc.perform(get("/admin/task/batch"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"));
    }

    @Test
    @DisplayName("batch page filters by taskType")
    void batchPageFilterByType() throws Exception {
        mockMvc.perform(get("/admin/task/batch")
                        .param("taskType", "WORD_AUDIO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"))
                .andExpect(model().attribute("taskType", "WORD_AUDIO"));
    }

    @Test
    @DisplayName("batch page filters by status")
    void batchPageFilterByStatus() throws Exception {
        mockMvc.perform(get("/admin/task/batch")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"))
                .andExpect(model().attribute("selectedStatus", "PENDING"));
    }

    @Test
    @DisplayName("batch page handles invalid status gracefully")
    void batchPageInvalidStatus() throws Exception {
        mockMvc.perform(get("/admin/task/batch")
                        .param("status", "INVALID_STATUS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"));
    }

    @Test
    @DisplayName("batch page supports sort parameter")
    void batchPageWithSort() throws Exception {
        mockMvc.perform(get("/admin/task/batch")
                        .param("sort", "createTime,desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/task/batch"))
                .andExpect(model().attribute("sortField", "createTime"))
                .andExpect(model().attribute("sortDirection", "desc"));
    }
}