package io.github.springwhale.platform.rbac.constant;

/**
 * RBAC-related shared constants.
 * <p>
 * Centralizes role codes, admin defaults, and authority prefixes
 * so they are not duplicated across the RBAC module.
 * </p>
 */
public final class RbacConstants {

    /**
     * Super administrator role code — users with this role bypass all permission checks
     */
    public static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    // ==================== Authority ====================
    /**
     * Wildcard authority granted to SUPER_ADMIN — matches all permissions
     */
    public static final String AUTHORITY_SUPER_ADMIN = "*";
    /**
     * Role authority prefix used by Spring Security
     */
    public static final String ROLE_PREFIX = "ROLE_";

    private RbacConstants() {
        // utility class
    }
}