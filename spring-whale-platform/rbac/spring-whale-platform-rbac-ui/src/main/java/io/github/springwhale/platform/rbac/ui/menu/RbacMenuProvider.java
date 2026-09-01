package io.github.springwhale.platform.rbac.ui.menu;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import java.util.List;

/**
 * Registers RBAC module menu items in the admin console sidebar.
 * <p>
 * Each leaf menu item carries a {@code permission} matching the
 * corresponding menu code so that the sidebar only shows entries
 * the current user is authorized to access.
 * </p>
 */
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
                MenuItem.group("rbac", "RBAC", "menu.rbac", "\uD83D\uDD10", 10),
                MenuItem.leaf("rbac-users", "rbac", "Users", "menu.rbac.user_management", "/admin/rbac/users", null, "rbac:user", 1),
                MenuItem.leaf("rbac-roles", "rbac", "Roles", "menu.rbac.role_management", "/admin/rbac/roles", null, "rbac:role", 2),
                MenuItem.leaf("rbac-menus", "rbac", "Menus", "menu.rbac.menu_management", "/admin/rbac/menus", null, "rbac:menu", 3),
                MenuItem.leaf("rbac-groups", "rbac", "Groups", "menu.rbac.group_management", "/admin/rbac/groups", null, "rbac:group", 4)
        );
    }

    @Override
    public int getOrder() {
        return 10;
    }
}