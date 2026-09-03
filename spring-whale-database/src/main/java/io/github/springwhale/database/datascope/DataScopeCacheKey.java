package io.github.springwhale.database.datascope;

/**
 * Centralized cache key factory for data scope caching.
 *
 * <p>Ensures consistent key format across {@code RBACDataScopeHandler}
 * and {@code SmartDataScopeHandler}.</p>
 *
 * <h3>Key categories</h3>
 * <table>
 *   <tr><th>Prefix</th><th>TTL</th><th>Purpose</th></tr>
 *   <tr><td>{@code skip:}</td><td>5 min</td><td>Primary skipDataScope cache</td></tr>
 *   <tr><td>{@code fallback:skip:}</td><td>30 min</td><td>Fallback when RBAC service is unreachable</td></tr>
 *   <tr><td>{@code dept:}</td><td>2 min</td><td>Primary resolveDeptIds cache</td></tr>
 *   <tr><td>{@code fallback:dept:}</td><td>30 min</td><td>Fallback when RBAC service is unreachable</td></tr>
 * </table>
 */
public final class DataScopeCacheKey {

    private static final String SKIP_PREFIX = "skip:";
    private static final String SKIP_TENANT_PREFIX = "skipTenant:";
    private static final String DEPT_PREFIX = "dept:";
    private static final String FALLBACK_SKIP_PREFIX = "fallback:skip:";
    private static final String FALLBACK_SKIP_TENANT_PREFIX = "fallback:skipTenant:";
    private static final String FALLBACK_DEPT_PREFIX = "fallback:dept:";

    private DataScopeCacheKey() {
    }

    /**
     * Key for the skipDataScope query.
     *
     * <p>Format: {@code skip:{userId}}</p>
     */
    public static String skipDataScope(Long userId) {
        return SKIP_PREFIX + userId;
    }

    /**
     * Key for the resolveDeptIds query.
     *
     * <p>Format: {@code dept:{userId}:{scopeType}:{module}}</p>
     */
    public static String resolveDeptIds(Long userId, DataScopeType scopeType, String module) {
        return DEPT_PREFIX + userId + ":" + scopeType.name() + ":" + (module != null ? module : "");
    }

    /**
     * Fallback key for skipDataScope with longer TTL.
     * Read when the primary fetch fails (RBAC service unreachable).
     *
     * <p>Format: {@code fallback:skip:{userId}}</p>
     */
    public static String fallbackSkipDataScope(Long userId) {
        return FALLBACK_SKIP_PREFIX + userId;
    }

    /**
     * Fallback key for resolveDeptIds with longer TTL.
     * Read when the primary fetch fails (RBAC service unreachable).
     *
     * <p>Format: {@code fallback:dept:{userId}:{scopeType}:{module}}</p>
     */
    public static String fallbackResolveDeptIds(Long userId, DataScopeType scopeType, String module) {
        return FALLBACK_DEPT_PREFIX + userId + ":" + scopeType.name() + ":" + (module != null ? module : "");
    }

    /**
     * Key for the skipTenantScope query.
     *
     * <p>Format: {@code skipTenant:{userId}}</p>
     */
    public static String skipTenantScope(Long userId) {
        return SKIP_TENANT_PREFIX + userId;
    }

    /**
     * Fallback key for skipTenantScope with longer TTL.
     * Read when the primary fetch fails (RBAC service unreachable).
     *
     * <p>Format: {@code fallback:skipTenant:{userId}}</p>
     */
    public static String fallbackSkipTenantScope(Long userId) {
        return FALLBACK_SKIP_TENANT_PREFIX + userId;
    }
}