package io.github.springwhale.platform.rbac.controller;

import tools.jackson.databind.ObjectMapper;
import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleDeptRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@Transactional
@DisplayName("RoleDeptController Integration Tests")
class RoleDeptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleDeptRepository roleDeptRepository;

    private RoleEntity role;

    @BeforeEach
    void setUp() {
        roleDeptRepository.deleteAll();
        roleRepository.deleteAll();

        role = new RoleEntity();
        role.setCode("MANAGER");
        role.setName("Manager");
        role.setStatus(1);
        role = roleRepository.save(role);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("get role dept list")
    void getDepts() throws Exception {
        RoleDeptEntity dept = new RoleDeptEntity();
        dept.setRoleId(role.getId());
        dept.setGroupId(100L);
        roleDeptRepository.save(dept);

        mockMvc.perform(get("/api/rbac/roles/{roleId}/depts", role.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(100));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("get role dept list - empty")
    void getDeptsEmpty() throws Exception {
        mockMvc.perform(get("/api/rbac/roles/{roleId}/depts", role.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
    @DisplayName("add depts to role")
    void addDepts() throws Exception {
        List<Long> deptIds = List.of(100L, 200L);

        mockMvc.perform(post("/api/rbac/roles/{roleId}/depts", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
    @DisplayName("add empty dept list to role")
    void addDeptsEmpty() throws Exception {
        List<Long> deptIds = List.of();

        mockMvc.perform(post("/api/rbac/roles/{roleId}/depts", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptIds)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
    @DisplayName("remove depts from role")
    void removeDepts() throws Exception {
        RoleDeptEntity dept = new RoleDeptEntity();
        dept.setRoleId(role.getId());
        dept.setGroupId(100L);
        roleDeptRepository.save(dept);

        List<Long> deptIds = List.of(100L);

        mockMvc.perform(delete("/api/rbac/roles/{roleId}/depts", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ok"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
    @DisplayName("remove empty dept list from role")
    void removeDeptsEmpty() throws Exception {
        List<Long> deptIds = List.of();

        mockMvc.perform(delete("/api/rbac/roles/{roleId}/depts", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deptIds)))
                .andExpect(status().isOk());
    }
}