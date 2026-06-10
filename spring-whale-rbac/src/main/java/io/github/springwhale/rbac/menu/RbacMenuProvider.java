package io.github.springwhale.rbac.menu;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers RBAC module menu items in the admin console sidebar.
 */
@Component
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
                MenuItem.group("rbac", "RBAC", "\uD83D\uDD10", 10),
                MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1),
                MenuItem.leaf("rbac-roles", "rbac", "Roles", "/admin/rbac/roles", 2),
                MenuItem.leaf("rbac-menus", "rbac", "Menus", "/admin/rbac/menus", 3),
                MenuItem.leaf("rbac-groups", "rbac", "Groups", "/admin/rbac/groups", 4)
        );
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
