package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dto.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private GroupEntity group;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        groupRepository.deleteAll();

        group = new GroupEntity();
        group.setCode("DEPT_01");
        group.setName("Dev Dept");
        group.setPath("/");
        group.setStatus(1);
        group = groupRepository.save(group);

        UserEntity user1 = new UserEntity();
        user1.setUsername("admin");
        user1.setPassword(passwordEncoder.encode("admin123"));
        user1.setRealName("Admin");
        user1.setEmail("admin@test.com");
        user1.setGroupId(group.getId());
        user1.setStatus(1);
        userRepository.save(user1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("user1");
        user2.setPassword(passwordEncoder.encode("password"));
        user2.setRealName("User One");
        user2.setGroupId(group.getId());
        user2.setStatus(1);
        userRepository.save(user2);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("paginated list of users")
    void findAll() throws Exception {
        mockMvc.perform(get("/api/rbac/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("search users by keyword")
    void findAllWithKeyword() throws Exception {
        mockMvc.perform(get("/api/rbac/users")
                        .param("page", "0")
                        .param("size", "20")
                        .param("keyword", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("get user by ID")
    void findById() throws Exception {
        UserEntity user = userRepository.findByUsername("user1").orElseThrow();

        mockMvc.perform(get("/api/rbac/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.realName").value("User One"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("non-existent user returns business error")
    void findByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/rbac/users/{id}", 9999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user:create"})
    @DisplayName("create user")
    void create() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRealName("New User");
        request.setEmail("new@test.com");
        request.setGroupId(group.getId());
        request.setStatus(1);

        mockMvc.perform(post("/api/rbac/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.realName").value("New User"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user:create"})
    @DisplayName("create user - empty username returns 500")
    void createWithEmptyUsername() throws Exception {
        UserRequest request = new UserRequest();
        request.setUsername("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/rbac/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("500"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user:update"})
    @DisplayName("update user")
    void update() throws Exception {
        UserEntity user = userRepository.findByUsername("user1").orElseThrow();

        UserRequest request = new UserRequest();
        request.setUsername("user1");
        request.setRealName("Updated User");
        request.setEmail("updated@test.com");
        request.setGroupId(group.getId());
        request.setStatus(1);

        mockMvc.perform(put("/api/rbac/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("Updated User"))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user:delete"})
    @DisplayName("delete user")
    void deleteUser() throws Exception {
        UserEntity user = userRepository.findByUsername("user1").orElseThrow();

        mockMvc.perform(delete("/api/rbac/users/{id}", user.getId()))
                .andExpect(status().isOk());
    }
}