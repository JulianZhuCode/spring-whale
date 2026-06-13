package io.github.springwhale.rbac.menu;

import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.menu.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Registers RBAC module menu items in the admin console sidebar.
 * <p>
 * Each leaf menu item carries a {@code permission} matching the
 * corresponding menu code so that the sidebar only shows entries
 * the current user is authorized to access.
 * </p>
 */
@Component
public class RbacMenuProvider implements AdminMenuProvider {

    @Override
    public List<MenuItem> getMenus() {
        return List.of(
                MenuItem.group("rbac", "RBAC", "\uD83D\uDD10", 10),
                MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", null, "rbac:user", 1),
                MenuItem.leaf("rbac-roles", "rbac", "Roles", "/admin/rbac/roles", null, "rbac:role", 2),
                MenuItem.leaf("rbac-menus", "rbac", "Menus", "/admin/rbac/menus", null, "rbac:menu", 3),
                MenuItem.leaf("rbac-groups", "rbac", "Groups", "/admin/rbac/groups", null, "rbac:group", 4)
        );
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
