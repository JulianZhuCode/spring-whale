package io.github.springwhale.rbac.constant;

/**
 * RBAC-related shared constants.
 * <p>
 * Centralizes role codes, admin defaults, and menu types
 * so they are not duplicated across the RBAC module.
 * </p>
 */
public final class RbacConstants {

    /**
     * Super administrator role code — users with this role bypass all permission checks
     */
    public static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    // ==================== Super Admin ====================
    /**
     * Super administrator role display name
     */
    public static final String SUPER_ADMIN_ROLE_NAME = "Super Administrator";
    /**
     * Default admin username
     */
    public static final String ADMIN_USERNAME = "admin";
    /**
     * Default admin password (will be BCrypt-encoded on first run)
     */
    public static final String ADMIN_PASSWORD = "admin";
    /**
     * Default admin display name
     */
    public static final String ADMIN_REAL_NAME = "Super Administrator";
    /**
     * Menu type: directory (container for menus)
     */
    public static final int MENU_TYPE_DIRECTORY = 1;

    // ==================== Menu Types ====================
    /**
     * Menu type: menu (navigable page)
     */
    public static final int MENU_TYPE_MENU = 2;
    /**
     * Menu type: button (action permission)
     */
    public static final int MENU_TYPE_BUTTON = 3;
    /**
     * Wildcard authority granted to SUPER_ADMIN — matches all permissions
     */
    public static final String AUTHORITY_SUPER_ADMIN = "*";

    // ==================== Authority ====================
    /**
     * Role authority prefix used by Spring Security
     */
    public static final String ROLE_PREFIX = "ROLE_";
    /**
     * Default root group code
     */
    public static final String ROOT_GROUP_CODE = "ROOT";

    // ==================== Default Root Group ====================
    /**
     * Default root group name
     */
    public static final String ROOT_GROUP_NAME = "Root Group";
    /**
     * Default root group description
     */
    public static final String ROOT_GROUP_DESCRIPTION = "Built-in root group";

    private RbacConstants() {
        // utility class
    }
}
