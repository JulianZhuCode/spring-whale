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
import io.github.springwhale.platform.rbac.dto.request.MenuRequest;
import io.github.springwhale.platform.rbac.enums.MenuType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@DisplayName("MenuController 集成测试")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    private MenuEntity systemMenu;
    private MenuEntity userMenu;
    private RoleEntity superAdminRole;
    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        roleMenuRepository.deleteAll();
        userRoleRepository.deleteAll();
        menuRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();

        systemMenu = new MenuEntity();
        systemMenu.setCode("system");
        systemMenu.setName("System");
        systemMenu.setType(MenuType.MENU);
        systemMenu.setPath("/system");
        systemMenu.setSort(1);
        systemMenu.setStatus(1);
        systemMenu = menuRepository.save(systemMenu);

        userMenu = new MenuEntity();
        userMenu.setCode("user");
        userMenu.setName("User Management");
        userMenu.setType(MenuType.MENU);
        userMenu.setPath("/user");
        userMenu.setParentId(systemMenu.getId());
        userMenu.setSort(1);
        userMenu.setStatus(1);
        userMenu = menuRepository.save(userMenu);

        superAdminRole = new RoleEntity();
        superAdminRole.setCode("SUPER_ADMIN");
        superAdminRole.setName("Super Admin");
        superAdminRole.setStatus(1);
        superAdminRole = roleRepository.save(superAdminRole);

        adminUser = new UserEntity();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRealName("Admin");
        adminUser.setStatus(1);
        adminUser = userRepository.save(adminUser);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(adminUser.getId());
        userRole.setRoleId(superAdminRole.getId());
        userRoleRepository.save(userRole);

        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setRoleId(superAdminRole.getId());
        roleMenu.setMenuId(systemMenu.getId());
        roleMenuRepository.save(roleMenu);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("分页查询菜单列表")
    void findAll() throws Exception {
        mockMvc.perform(get("/api/rbac/menus")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("根据关键字搜索菜单")
    void findAllWithKeyword() throws Exception {
        mockMvc.perform(get("/api/rbac/menus")
                        .param("page", "0")
                        .param("size", "20")
                        .param("keyword", "User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("User Management"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("根据ID查询菜单")
    void findById() throws Exception {
        mockMvc.perform(get("/api/rbac/menus/{id}", systemMenu.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("system"))
                .andExpect(jsonPath("$.data.name").value("System"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("查询不存在的菜单 - 返回业务错误")
    void findByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/rbac/menus/{id}", 9999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu:create"})
    @DisplayName("创建菜单")
    void create() throws Exception {
        MenuRequest request = new MenuRequest();
        request.setCode("settings");
        request.setName("Settings");
        request.setType(MenuType.MENU);
        request.setPath("/settings");
        request.setParentId(systemMenu.getId());
        request.setSort(2);
        request.setStatus(1);

        mockMvc.perform(post("/api/rbac/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("settings"))
                .andExpect(jsonPath("$.data.name").value("Settings"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu:update"})
    @DisplayName("更新菜单")
    void update() throws Exception {
        MenuRequest request = new MenuRequest();
        request.setCode(userMenu.getCode());
        request.setName("Updated Menu");
        request.setType(MenuType.MENU);
        request.setPath("/user");
        request.setStatus(1);

        mockMvc.perform(put("/api/rbac/menus/{id}", userMenu.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Menu"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu:delete"})
    @DisplayName("删除菜单")
    void deleteMenu() throws Exception {
        mockMvc.perform(delete("/api/rbac/menus/{id}", userMenu.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("获取菜单树 - 超级管理员返回全部菜单")
    void tree() throws Exception {
        mockMvc.perform(get("/api/rbac/menus/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("System"))
                .andExpect(jsonPath("$.data[0].children").isArray());
    }
}