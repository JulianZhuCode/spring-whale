package io.github.springwhale.platform.rbac.controller;

import tools.jackson.databind.ObjectMapper;
import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dto.request.RoleRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@Transactional
@DisplayName("RoleController Integration Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();

        RoleEntity adminRole = new RoleEntity();
        adminRole.setCode("ADMIN");
        adminRole.setName("Admin");
        adminRole.setStatus(1);
        roleRepository.save(adminRole);

        RoleEntity userRole = new RoleEntity();
        userRole.setCode("USER");
        userRole.setName("User");
        userRole.setStatus(1);
        roleRepository.save(userRole);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("paginated list of roles")
    void findAll() throws Exception {
        mockMvc.perform(get("/api/rbac/roles")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("search role by keyword")
    void findAllWithKeyword() throws Exception {
        mockMvc.perform(get("/api/rbac/roles")
                        .param("page", "0")
                        .param("size", "20")
                        .param("keyword", "Admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("get role by ID")
    void findById() throws Exception {
        RoleEntity role = roleRepository.findAll().get(0);

        mockMvc.perform(get("/api/rbac/roles/{id}", role.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value(role.getCode()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("non-existent role returns business error")
    void findByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/rbac/roles/{id}", 9999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:create"})
    @DisplayName("create role")
    void create() throws Exception {
        RoleRequest request = new RoleRequest();
        request.setCode("MANAGER");
        request.setName("Manager");
        request.setStatus(1);

        mockMvc.perform(post("/api/rbac/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("MANAGER"))
                .andExpect(jsonPath("$.data.name").value("Manager"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
    @DisplayName("update role")
    void update() throws Exception {
        RoleEntity role = roleRepository.findAll().get(0);

        RoleRequest request = new RoleRequest();
        request.setCode(role.getCode());
        request.setName("Updated Role");
        request.setStatus(1);

        mockMvc.perform(put("/api/rbac/roles/{id}", role.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Role"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role:delete"})
    @DisplayName("delete role")
    void deleteRole() throws Exception {
        RoleEntity role = roleRepository.findAll().get(0);

        mockMvc.perform(delete("/api/rbac/roles/{id}", role.getId()))
                .andExpect(status().isOk());
    }
}