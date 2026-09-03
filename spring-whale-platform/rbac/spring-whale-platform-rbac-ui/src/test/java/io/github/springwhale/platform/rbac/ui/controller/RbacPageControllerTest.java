package io.github.springwhale.platform.rbac.ui.controller;

import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.MenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.enums.MenuType;
import io.github.springwhale.platform.rbac.ui.TestSecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@DisplayName("RbacPageController Integration Tests")
class RbacPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        menuRepository.deleteAll();
        groupRepository.deleteAll();

        UserEntity user1 = new UserEntity();
        user1.setUsername("admin");
        user1.setPassword("password");
        user1.setRealName("Admin");
        user1.setStatus(1);
        userRepository.save(user1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("user1");
        user2.setPassword("password");
        user2.setRealName("User One");
        user2.setStatus(1);
        userRepository.save(user2);

        UserEntity user3 = new UserEntity();
        user3.setUsername("user2");
        user3.setPassword("password");
        user3.setRealName("User Two");
        user3.setStatus(0);
        userRepository.save(user3);

        RoleEntity role1 = new RoleEntity();
        role1.setCode("ADMIN");
        role1.setName("Admin");
        role1.setStatus(1);
        roleRepository.save(role1);

        RoleEntity role2 = new RoleEntity();
        role2.setCode("USER");
        role2.setName("User");
        role2.setStatus(1);
        roleRepository.save(role2);

        RoleEntity role3 = new RoleEntity();
        role3.setCode("GUEST");
        role3.setName("Guest");
        role3.setStatus(0);
        roleRepository.save(role3);

        MenuEntity menu1 = new MenuEntity();
        menu1.setCode("system");
        menu1.setName("System");
        menu1.setType(MenuType.DIRECTORY);
        menu1.setPath("/system");
        menu1.setSort(1);
        menu1.setStatus(1);
        menu1.setVisible(1);
        menuRepository.save(menu1);

        MenuEntity menu2 = new MenuEntity();
        menu2.setCode("user");
        menu2.setName("User Management");
        menu2.setType(MenuType.MENU);
        menu2.setPath("/user");
        menu2.setParentId(menu1.getId());
        menu2.setSort(1);
        menu2.setStatus(1);
        menu2.setVisible(1);
        menuRepository.save(menu2);

        MenuEntity menu3 = new MenuEntity();
        menu3.setCode("disabled_menu");
        menu3.setName("Disabled Menu");
        menu3.setType(MenuType.MENU);
        menu3.setPath("/disabled");
        menu3.setSort(2);
        menu3.setStatus(0);
        menu3.setVisible(1);
        menuRepository.save(menu3);

        GroupEntity group1 = new GroupEntity();
        group1.setCode("ROOT");
        group1.setName("Root Dept");
        group1.setPath("/");
        group1.setStatus(1);
        groupRepository.save(group1);

        GroupEntity group2 = new GroupEntity();
        group2.setCode("CHILD");
        group2.setName("Child Dept");
        group2.setParentId(group1.getId());
        group2.setPath("/" + group1.getId() + "/");
        group2.setStatus(1);
        groupRepository.save(group2);

        GroupEntity group3 = new GroupEntity();
        group3.setCode("DISABLED");
        group3.setName("Disabled Dept");
        group3.setPath("/");
        group3.setStatus(0);
        groupRepository.save(group3);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("users page - returns correct view and model")
    void usersPage() throws Exception {
        mockMvc.perform(get("/admin/rbac/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("keyword", nullValue()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("users page - pagination parameters")
    void usersPageWithPagination() throws Exception {
        mockMvc.perform(get("/admin/rbac/users")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/users"))
                .andExpect(model().attributeExists("users"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("users page - keyword filter")
    void usersPageWithKeyword() throws Exception {
        mockMvc.perform(get("/admin/rbac/users")
                        .param("keyword", "User One"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/users"))
                .andExpect(model().attributeExists("keyword"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:user"})
    @DisplayName("users page - status filter")
    void usersPageWithStatusFilter() throws Exception {
        mockMvc.perform(get("/admin/rbac/users")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/users"))
                .andExpect(model().attribute("selectedStatus", "0"));
    }

    @Test
    @DisplayName("users page - unauthorized redirects to login")
    void usersPageUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/rbac/users"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("roles page - returns correct view and model")
    void rolesPage() throws Exception {
        mockMvc.perform(get("/admin/rbac/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/roles"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("keyword", nullValue()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("roles page - keyword filter")
    void rolesPageWithKeyword() throws Exception {
        mockMvc.perform(get("/admin/rbac/roles")
                        .param("keyword", "Guest"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/roles"))
                .andExpect(model().attributeExists("keyword"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:role"})
    @DisplayName("roles page - status filter")
    void rolesPageWithStatusFilter() throws Exception {
        mockMvc.perform(get("/admin/rbac/roles")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/roles"))
                .andExpect(model().attribute("selectedStatus", "0"));
    }

    @Test
    @DisplayName("roles page - unauthorized redirects to login")
    void rolesPageUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/rbac/roles"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("menus page - returns correct view and model")
    void menusPage() throws Exception {
        mockMvc.perform(get("/admin/rbac/menus"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/menus"))
                .andExpect(model().attributeExists("menus"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("keyword", nullValue()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("menus page - keyword filter")
    void menusPageWithKeyword() throws Exception {
        mockMvc.perform(get("/admin/rbac/menus")
                        .param("keyword", "Disabled"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/menus"))
                .andExpect(model().attributeExists("keyword"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("menus page - type filter")
    void menusPageWithTypeFilter() throws Exception {
        mockMvc.perform(get("/admin/rbac/menus")
                        .param("type", "DIRECTORY"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/menus"))
                .andExpect(model().attribute("selectedType", "DIRECTORY"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:menu"})
    @DisplayName("menus page - status filter")
    void menusPageWithStatusFilter() throws Exception {
        mockMvc.perform(get("/admin/rbac/menus")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/menus"))
                .andExpect(model().attribute("selectedStatus", "0"));
    }

    @Test
    @DisplayName("menus page - unauthorized redirects to login")
    void menusPageUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/rbac/menus"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("groups page - returns correct view and model")
    void groupsPage() throws Exception {
        mockMvc.perform(get("/admin/rbac/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/groups"))
                .andExpect(model().attributeExists("groups"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("keyword", nullValue()));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("groups page - keyword filter")
    void groupsPageWithKeyword() throws Exception {
        mockMvc.perform(get("/admin/rbac/groups")
                        .param("keyword", "Child"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/groups"))
                .andExpect(model().attributeExists("keyword"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"rbac:group"})
    @DisplayName("groups page - status filter")
    void groupsPageWithStatusFilter() throws Exception {
        mockMvc.perform(get("/admin/rbac/groups")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/groups"))
                .andExpect(model().attribute("selectedStatus", "0"));
    }

    @Test
    @DisplayName("groups page - unauthorized redirects to login")
    void groupsPageUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/rbac/groups"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"*"})
    @DisplayName("wildcard authority can access all pages")
    void wildcardAuthorityAccessAllPages() throws Exception {
        mockMvc.perform(get("/admin/rbac/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/users"));

        mockMvc.perform(get("/admin/rbac/roles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/roles"));

        mockMvc.perform(get("/admin/rbac/menus"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/menus"));

        mockMvc.perform(get("/admin/rbac/groups"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rbac/groups"));
    }
}