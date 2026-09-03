package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfiguration.class)
@TestPropertySource(properties = {
        "spring.whale.database.datascope.expose-remote-api=true",
        "spring.whale.database.datascope.enabled=false"
})
@DisplayName("DataScopeController 集成测试")
class DataScopeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RBACDataScopeHandler handler;

    @Test
    @DisplayName("skipDataScope - 超级管理员")
    void skipDataScope() throws Exception {
        when(handler.skipDataScope(1)).thenReturn(true);

        mockMvc.perform(get("/api/rbac/datascope/skip/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skip").value(true));
    }

    @Test
    @DisplayName("skipDataScope - 普通用户")
    void skipDataScopeNormalUser() throws Exception {
        when(handler.skipDataScope(2)).thenReturn(false);

        mockMvc.perform(get("/api/rbac/datascope/skip/{userId}", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skip").value(false));
    }

    @Test
    @DisplayName("skipTenantScope - 返回true")
    void skipTenantScope() throws Exception {
        when(handler.skipTenantScope(1)).thenReturn(true);

        mockMvc.perform(get("/api/rbac/datascope/skip-tenant/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skip").value(true));
    }

    @Test
    @DisplayName("resolveDeptIds - DEPT类型")
    void resolveDeptIds() throws Exception {
        when(handler.resolveDeptIds(eq(1), eq(DataScopeType.DEPT), isNull()))
                .thenReturn(List.of(10));

        mockMvc.perform(get("/api/rbac/datascope/resolve/{userId}", 1)
                        .param("scopeType", "DEPT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptIds[0]").value(10));
    }

    @Test
    @DisplayName("resolveDeptIds - DEPT_AND_CHILD类型")
    void resolveDeptIdsDeptAndChild() throws Exception {
        when(handler.resolveDeptIds(eq(1), eq(DataScopeType.DEPT_AND_CHILD), isNull()))
                .thenReturn(List.of(10, 11, 12));

        mockMvc.perform(get("/api/rbac/datascope/resolve/{userId}", 1)
                        .param("scopeType", "DEPT_AND_CHILD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptIds.length()").value(3));
    }

    @Test
    @DisplayName("resolveDeptIds - 带module参数")
    void resolveDeptIdsWithModule() throws Exception {
        when(handler.resolveDeptIds(eq(1), eq(DataScopeType.CUSTOM), eq("rbac:user")))
                .thenReturn(List.of(100));

        mockMvc.perform(get("/api/rbac/datascope/resolve/{userId}", 1)
                        .param("scopeType", "CUSTOM")
                        .param("module", "rbac:user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptIds[0]").value(100));
    }

    @Test
    @DisplayName("resolveDeptIds - 空结果")
    void resolveDeptIdsEmpty() throws Exception {
        when(handler.resolveDeptIds(eq(1), eq(DataScopeType.SELF), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/rbac/datascope/resolve/{userId}", 1)
                        .param("scopeType", "SELF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptIds").isArray())
                .andExpect(jsonPath("$.data.deptIds").isEmpty());
    }

    @Test
    @DisplayName("清除用户缓存")
    void evictCache() throws Exception {
        mockMvc.perform(delete("/api/rbac/datascope/cache/{userId}", 1))
                .andExpect(status().isOk());
    }
}