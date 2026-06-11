package io.github.springwhale.rbac.config;

import io.github.springwhale.rbac.constant.RbacConstants;
import io.github.springwhale.rbac.entity.GroupEntity;
import io.github.springwhale.rbac.entity.MenuEntity;
import io.github.springwhale.rbac.entity.RoleEntity;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.entity.UserRoleEntity;
import io.github.springwhale.rbac.repository.GroupRepository;
import io.github.springwhale.rbac.repository.MenuRepository;
import io.github.springwhale.rbac.repository.RoleRepository;
import io.github.springwhale.rbac.repository.UserRepository;
import io.github.springwhale.rbac.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



/**
 * Initializes default admin account, RBAC menus, and permissions on startup.
 * <p>
 * Menu hierarchy:
 * <pre>
 * System (directory)
 *   └── RBAC (directory)
 *       ├── User Management (menu)
 *       │   ├── User Create (button)
 *       │   ├── User Update (button)
 *       │   └── User Delete (button)
 *       ├── Role Management (menu)
 *       │   ├── Role Create (button)
 *       │   ├── Role Update (button)
 *       │   └── Role Delete (button)
 *       ├── Menu Management (menu)
 *       │   ├── Menu Create (button)
 *       │   ├── Menu Update (button)
 *       │   └── Menu Delete (button)
 *       └── Group Management (menu)
 *           ├── Group Create (button)
 *           ├── Group Update (button)
 *           └── Group Delete (button)
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final MenuRepository menuRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        GroupEntity rootGroup = initRootGroup();
        RoleEntity superAdminRole = initSuperAdminRole(rootGroup);
        initAdminUser(superAdminRole, rootGroup);
        initRbacMenus(superAdminRole);
    }

    // ==================== Root Group ====================

    private GroupEntity initRootGroup() {
        return groupRepository.findByCode(RbacConstants.ROOT_GROUP_CODE)
                .orElseGet(() -> {
                    GroupEntity group = new GroupEntity();
                    group.setCode(RbacConstants.ROOT_GROUP_CODE);
                    group.setName(RbacConstants.ROOT_GROUP_NAME);
                    group.setDescription(RbacConstants.ROOT_GROUP_DESCRIPTION);
                    group.setSort(0);
                    group.setStatus(1);
                    log.info("Created root group '{}'", RbacConstants.ROOT_GROUP_CODE);
                    return groupRepository.save(group);
                });
    }

    // ==================== Role ====================

    private RoleEntity initSuperAdminRole(GroupEntity rootGroup) {
        return roleRepository.findByCode(RbacConstants.SUPER_ADMIN_ROLE_CODE)
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setCode(RbacConstants.SUPER_ADMIN_ROLE_CODE);
                    role.setName(RbacConstants.SUPER_ADMIN_ROLE_NAME);
                    role.setDescription("Built-in super administrator role with full permissions");
                    role.setStatus(1);
                    role.setSort(0);
                    role.setGroupId(rootGroup.getId());
                    log.info("Created SUPER_ADMIN role");
                    return roleRepository.save(role);
                });
    }

    // ==================== Admin User ====================

    private void initAdminUser(RoleEntity superAdminRole, GroupEntity rootGroup) {
        UserEntity admin = userRepository.findByUsername(RbacConstants.ADMIN_USERNAME).orElse(null);

        if (admin == null) {
            log.info("Initializing default admin user '{}'...", RbacConstants.ADMIN_USERNAME);
            admin = new UserEntity();
            admin.setUsername(RbacConstants.ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(RbacConstants.ADMIN_PASSWORD));
            admin.setRealName(RbacConstants.ADMIN_REAL_NAME);
            admin.setStatus(1);
            admin.setGroupId(rootGroup.getId());
            admin = userRepository.save(admin);

            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(superAdminRole.getId());
            userRoleRepository.save(userRole);

            log.info("Default admin user '{}' created successfully (id={})", RbacConstants.ADMIN_USERNAME, admin.getId());
        } else {
            // Ensure existing admin is in the root group
            if (admin.getGroupId() == null) {
                admin.setGroupId(rootGroup.getId());
            }
            String encodedPassword = passwordEncoder.encode(RbacConstants.ADMIN_PASSWORD);
            if (!passwordEncoder.matches(RbacConstants.ADMIN_PASSWORD, admin.getPassword())) {
                log.info("Admin user '{}' exists but password needs re-encoding", RbacConstants.ADMIN_USERNAME);
                admin.setPassword(encodedPassword);
            } else {
                log.info("Admin user '{}' already exists with valid password", RbacConstants.ADMIN_USERNAME);
            }
            userRepository.save(admin);

            if (!userRoleRepository.findByUserId(admin.getId()).stream()
                    .anyMatch(ur -> ur.getRoleId().equals(superAdminRole.getId()))) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(admin.getId());
                userRole.setRoleId(superAdminRole.getId());
                userRoleRepository.save(userRole);
                log.info("Assigned SUPER_ADMIN role to existing admin user");
            }
        }
    }

    // ==================== RBAC Menus ====================

    private void initRbacMenus(RoleEntity superAdminRole) {
        // Skip if menus already exist
        if (menuRepository.findByCode("system").isPresent()) {
            log.info("RBAC menus already initialized, skipping");
            return;
        }

        log.info("Initializing RBAC menus...");

        // Level 0: System root directory
        MenuEntity systemDir = createMenu(null, "system", "System", RbacConstants.MENU_TYPE_DIRECTORY,
                null, null, null, "menu", 0);

        // Level 1: RBAC directory
        MenuEntity rbacDir = createMenu(systemDir.getId(), "rbac", "RBAC", RbacConstants.MENU_TYPE_DIRECTORY,
                null, null, null, "shield", 1);

        // Level 2: Four management menus with their button permissions
        createMenuWithButtons(rbacDir, "rbac:user", "User Management",
                "/admin/rbac/users", 2);
        createMenuWithButtons(rbacDir, "rbac:role", "Role Management",
                "/admin/rbac/roles", 3);
        createMenuWithButtons(rbacDir, "rbac:menu", "Menu Management",
                "/admin/rbac/menus", 4);
        createMenuWithButtons(rbacDir, "rbac:group", "Group Management",
                "/admin/rbac/groups", 5);

        log.info("RBAC menus initialized successfully (SUPER_ADMIN has all permissions via wildcard)");
    }

    private MenuEntity createMenu(Integer parentId, String code, String name, int type,
                                   String path, String component, String permission,
                                   String icon, int sort) {
        MenuEntity menu = new MenuEntity();
        menu.setParentId(parentId);
        menu.setCode(code);
        menu.setName(name);
        menu.setType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setVisible(1);
        menu.setStatus(1);
        return menuRepository.save(menu);
    }

    /**
     * Creates a menu item and its standard CRUD button permissions.
     */
    private void createMenuWithButtons(MenuEntity parent, String baseCode, String name,
                                        String path, int sort) {
        // Main menu
        MenuEntity menu = createMenu(parent.getId(), baseCode, name, RbacConstants.MENU_TYPE_MENU,
                path, null, null, "file-text", sort);

        // Standard CRUD button permissions
        createMenu(menu.getId(), baseCode + ":create", name + " Create", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":create", null, 1);
        createMenu(menu.getId(), baseCode + ":update", name + " Update", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":update", null, 2);
        createMenu(menu.getId(), baseCode + ":delete", name + " Delete", RbacConstants.MENU_TYPE_BUTTON,
                null, null, baseCode + ":delete", null, 3);
    }

}
