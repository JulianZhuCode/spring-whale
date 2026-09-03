package io.github.springwhale.platform.rbac.controller;

import tools.jackson.databind.ObjectMapper;
import io.github.springwhale.platform.rbac.TestSecurityConfiguration;
import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.MenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@DisplayName("RBAC 关联关系 Controller 集成测试")
class AdminAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    private UserEntity testUser;
    private RoleEntity testRole;
    private MenuEntity testMenu;

    @BeforeEach
    void setUp() {
        roleMenuRepository.deleteAll();
        userRoleRepository.deleteAll();
        menuRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRealName("Test User");
        testUser.setStatus(1);
        testUser = userRepository.save(testUser);

        testRole = new RoleEntity();
        testRole.setCode("TEST_ROLE");
        testRole.setName("Test Role");
        testRole.setStatus(1);
        testRole = roleRepository.save(testRole);

        testMenu = new MenuEntity();
        testMenu.setCode("test_menu");
        testMenu.setName("Test Menu");
        testMenu.setType(io.github.springwhale.platform.rbac.enums.MenuType.MENU);
        testMenu.setPath("/test");
        testMenu.setStatus(1);
        testMenu = menuRepository.save(testMenu);
    }

    @Nested
    @DisplayName("UserRoleController")
    class UserRoleTests {

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:user"})
        @DisplayName("获取用户角色ID列表")
        void getRoles() throws Exception {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(testUser.getId());
            userRole.setRoleId(testRole.getId());
            userRoleRepository.save(userRole);

            mockMvc.perform(get("/api/rbac/users/{userId}/roles", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0]").value(testRole.getId()));
        }

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:user:update"})
        @DisplayName("批量添加用户角色")
        void addRoles() throws Exception {
            mockMvc.perform(post("/api/rbac/users/{userId}/roles", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(testRole.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ok"));
        }

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:user:update"})
        @DisplayName("批量移除用户角色")
        void removeRoles() throws Exception {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(testUser.getId());
            userRole.setRoleId(testRole.getId());
            userRoleRepository.save(userRole);

            mockMvc.perform(delete("/api/rbac/users/{userId}/roles", testUser.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(testRole.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ok"));
        }
    }

    @Nested
    @DisplayName("RoleMenuController")
    class RoleMenuTests {

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:role"})
        @DisplayName("获取角色菜单ID列表")
        void getMenus() throws Exception {
            RoleMenuEntity roleMenu = new RoleMenuEntity();
            roleMenu.setRoleId(testRole.getId());
            roleMenu.setMenuId(testMenu.getId());
            roleMenuRepository.save(roleMenu);

            mockMvc.perform(get("/api/rbac/roles/{roleId}/menus", testRole.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0]").value(testMenu.getId()));
        }

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
        @DisplayName("批量添加角色菜单")
        void addMenus() throws Exception {
            mockMvc.perform(post("/api/rbac/roles/{roleId}/menus", testRole.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(testMenu.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ok"));
        }

        @Test
        @WithMockUser(username = "admin", authorities = {"rbac:role:update"})
        @DisplayName("批量移除角色菜单")
        void removeMenus() throws Exception {
            RoleMenuEntity roleMenu = new RoleMenuEntity();
            roleMenu.setRoleId(testRole.getId());
            roleMenu.setMenuId(testMenu.getId());
            roleMenuRepository.save(roleMenu);

            mockMvc.perform(delete("/api/rbac/roles/{roleId}/menus", testRole.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(testMenu.getId()))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("ok"));
        }
    }
}