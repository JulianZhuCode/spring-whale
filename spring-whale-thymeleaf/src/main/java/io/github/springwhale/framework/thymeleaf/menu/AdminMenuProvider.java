package io.github.springwhale.framework.thymeleaf.menu;

import java.util.List;

/**
 * SPI interface for business modules to register admin console menus.
 * <p>
 * Each business module (e.g. RBAC, Dict) implements this interface
 * and registers as a Spring Bean. The admin console will automatically
 * discover all providers and render the sidebar menu tree.
 * </p>
 *
 * <pre>{@code
 * @Component
 * public class RbacMenuProvider implements AdminMenuProvider {
 *     @Override
 *     public List<MenuItem> getMenus() {
 *         return List.of(
 *             MenuItem.group("rbac", "RBAC", "shield", 10),
 *             MenuItem.leaf("rbac-users", "rbac", "Users", "/admin/rbac/users", 1),
 *             MenuItem.leaf("rbac-roles", "rbac", "Roles", "/admin/rbac/roles", 2)
 *         );
 *     }
 *
 *     @Override
 *     public int getOrder() { return 10; }
 * }
 * }</pre>
 */
public interface AdminMenuProvider {

    /**
     * Returns the menu items contributed by this module.
     * Both group (parent) and leaf (child) items are returned together.
     */
    List<MenuItem> getMenus();

    /**
     * Sort order among providers. Lower values appear first.
     */
    default int getOrder() {
        return 0;
    }
}
