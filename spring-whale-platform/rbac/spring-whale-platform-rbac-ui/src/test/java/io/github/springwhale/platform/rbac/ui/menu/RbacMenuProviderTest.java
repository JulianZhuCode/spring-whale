package io.github.springwhale.platform.rbac.ui.menu;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
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
import io.github.springwhale.platform.rbac.enums.MenuType;
import io.github.springwhale.platform.rbac.ui.TestSecurityConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestSecurityConfiguration.class)
@DisplayName("RbacMenuProvider Integration Tests")
class RbacMenuProviderTest {

    @Autowired
    private RbacMenuProvider rbacMenuProvider;

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

    private UserEntity adminUser;
    private UserEntity normalUser;
    private RoleEntity superAdminRole;
    private RoleEntity normalRole;
    private MenuEntity systemMenu;
    private MenuEntity userMenu;
    private MenuEntity roleMenu;
    private MenuEntity hiddenMenu;
    private MenuEntity disabledMenu;
    private MenuEntity buttonPermission;

    @BeforeEach
    void setUp() {
        roleMenuRepository.deleteAll();
        userRoleRepository.deleteAll();
        menuRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new UserEntity();
        adminUser.setUsername("admin");
        adminUser.setPassword("password");
        adminUser.setRealName("Admin");
        adminUser.setStatus(1);
        adminUser = userRepository.save(adminUser);

        normalUser = new UserEntity();
        normalUser.setUsername("normal");
        normalUser.setPassword("password");
        normalUser.setRealName("Normal User");
        normalUser.setStatus(1);
        normalUser = userRepository.save(normalUser);

        superAdminRole = new RoleEntity();
        superAdminRole.setCode(RbacConstants.SUPER_ADMIN_ROLE_CODE);
        superAdminRole.setName("Super Admin");
        superAdminRole.setStatus(1);
        superAdminRole = roleRepository.save(superAdminRole);

        normalRole = new RoleEntity();
        normalRole.setCode("NORMAL");
        normalRole.setName("Normal");
        normalRole.setStatus(1);
        normalRole = roleRepository.save(normalRole);

        systemMenu = new MenuEntity();
        systemMenu.setCode("system");
        systemMenu.setName("System");
        systemMenu.setType(MenuType.DIRECTORY);
        systemMenu.setSort(1);
        systemMenu.setVisible(1);
        systemMenu.setStatus(1);
        systemMenu = menuRepository.save(systemMenu);

        userMenu = new MenuEntity();
        userMenu.setCode("user");
        userMenu.setName("User Management");
        userMenu.setType(MenuType.MENU);
        userMenu.setPath("/user");
        userMenu.setParentId(systemMenu.getId());
        userMenu.setSort(1);
        userMenu.setVisible(1);
        userMenu.setStatus(1);
        userMenu = menuRepository.save(userMenu);

        roleMenu = new MenuEntity();
        roleMenu.setCode("role");
        roleMenu.setName("Role Management");
        roleMenu.setType(MenuType.MENU);
        roleMenu.setPath("/role");
        roleMenu.setParentId(systemMenu.getId());
        roleMenu.setSort(2);
        roleMenu.setVisible(1);
        roleMenu.setStatus(1);
        roleMenu.setPermission("rbac:role");
        roleMenu = menuRepository.save(roleMenu);

        hiddenMenu = new MenuEntity();
        hiddenMenu.setCode("hidden");
        hiddenMenu.setName("Hidden Menu");
        hiddenMenu.setType(MenuType.MENU);
        hiddenMenu.setPath("/hidden");
        hiddenMenu.setSort(3);
        hiddenMenu.setVisible(0);
        hiddenMenu.setStatus(1);
        hiddenMenu = menuRepository.save(hiddenMenu);

        disabledMenu = new MenuEntity();
        disabledMenu.setCode("disabled");
        disabledMenu.setName("Disabled Menu");
        disabledMenu.setType(MenuType.MENU);
        disabledMenu.setPath("/disabled");
        disabledMenu.setSort(4);
        disabledMenu.setVisible(1);
        disabledMenu.setStatus(0);
        disabledMenu = menuRepository.save(disabledMenu);

        buttonPermission = new MenuEntity();
        buttonPermission.setCode("user:create");
        buttonPermission.setName("Create User");
        buttonPermission.setType(MenuType.BUTTON);
        buttonPermission.setPermission("rbac:user:create");
        buttonPermission.setSort(1);
        buttonPermission.setVisible(1);
        buttonPermission.setStatus(1);
        buttonPermission = menuRepository.save(buttonPermission);

        UserRoleEntity adminUserRole = new UserRoleEntity();
        adminUserRole.setUserId(adminUser.getId());
        adminUserRole.setRoleId(superAdminRole.getId());
        userRoleRepository.save(adminUserRole);

        UserRoleEntity normalUserRole = new UserRoleEntity();
        normalUserRole.setUserId(normalUser.getId());
        normalUserRole.setRoleId(normalRole.getId());
        userRoleRepository.save(normalUserRole);

        RoleMenuEntity normalRoleSystemMenu = new RoleMenuEntity();
        normalRoleSystemMenu.setRoleId(normalRole.getId());
        normalRoleSystemMenu.setMenuId(systemMenu.getId());
        roleMenuRepository.save(normalRoleSystemMenu);

        RoleMenuEntity normalRoleMenu = new RoleMenuEntity();
        normalRoleMenu.setRoleId(normalRole.getId());
        normalRoleMenu.setMenuId(userMenu.getId());
        roleMenuRepository.save(normalRoleMenu);
    }

    @AfterEach
    void tearDown() {
        AuthenticationContextHolder.clearContext();
    }

    @Test
    @DisplayName("super admin sees all visible menus")
    void superAdminSeesAllVisibleMenus() {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(adminUser.getId(), "admin", null));

        List<MenuItem> menus = rbacMenuProvider.getMenus();

        assertNotNull(menus);
        assertFalse(menus.isEmpty());
        assertTrue(menus.stream().anyMatch(m -> "system".equals(m.getKey())));
        assertTrue(menus.stream().anyMatch(m -> "user".equals(m.getKey())));
        assertTrue(menus.stream().anyMatch(m -> "role".equals(m.getKey())));
        assertFalse(menus.stream().anyMatch(m -> "hidden".equals(m.getKey())));
        assertFalse(menus.stream().anyMatch(m -> "disabled".equals(m.getKey())));
        assertFalse(menus.stream().anyMatch(m -> "user:create".equals(m.getKey())));
    }

    @Test
    @DisplayName("normal user sees only assigned menus")
    void normalUserSeesOnlyAssignedMenus() {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(normalUser.getId(), "normal", null));

        List<MenuItem> menus = rbacMenuProvider.getMenus();

        assertNotNull(menus);
        assertTrue(menus.stream().anyMatch(m -> "user".equals(m.getKey())));
        assertFalse(menus.stream().anyMatch(m -> "role".equals(m.getKey())));
    }

    @Test
    @DisplayName("unauthenticated user returns empty list")
    void unauthenticatedUserReturnsEmpty() {
        List<MenuItem> menus = rbacMenuProvider.getMenus();

        assertNotNull(menus);
        assertTrue(menus.isEmpty());
    }

    @Test
    @DisplayName("menu provider order is 10")
    void menuProviderOrder() {
        assertEquals(10, rbacMenuProvider.getOrder());
    }

    @Test
    @DisplayName("directory type menus are returned as groups")
    void directoryTypeMenusReturnedAsGroups() {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(adminUser.getId(), "admin", null));

        List<MenuItem> menus = rbacMenuProvider.getMenus();

        MenuItem systemItem = menus.stream()
                .filter(m -> "system".equals(m.getKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(systemItem);
        assertFalse(systemItem.isLeaf());
    }

    @Test
    @DisplayName("menu type menus are returned as leaf items")
    void menuTypeMenusReturnedAsLeafItems() {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(adminUser.getId(), "admin", null));

        List<MenuItem> menus = rbacMenuProvider.getMenus();

        MenuItem userItem = menus.stream()
                .filter(m -> "user".equals(m.getKey()))
                .findFirst()
                .orElse(null);
        assertNotNull(userItem);
        assertTrue(userItem.isLeaf());
        assertEquals("/user", userItem.getUrl());
    }
}