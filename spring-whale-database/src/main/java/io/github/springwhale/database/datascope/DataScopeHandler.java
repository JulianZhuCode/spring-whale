package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.utils.AuthUtil;

import java.util.List;

/**
 * SPI for resolving data scope parameters (user ID, department IDs, tenant ID).
 *
 * <p>Default implementations delegate to {@link AuthUtil}. Override
 * {@link #resolveDeptIds(DataScopeType, String)} to provide custom department
 * resolution logic (e.g., from a database or cache).</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @Component
 * public class MyDataScopeHandler implements DataScopeHandler {
 *     public List<Object> resolveDeptIds(DataScopeType type, String module) {
 *         return deptService.getChildDeptIds(AuthUtil.getUserId());
 *     }
 * }
 * }</pre>
 */
public interface DataScopeHandler {

    /**
     * Whether to skip department/user data scope filtering entirely.
     * When {@code true}, neither {@link #resolveDeptIds} nor {@link #resolveUserId}
     * is called, and no WHERE clause is injected for {@code @DeptIdField} / {@code @UserIdField}.
     *
     * <p>Typical usage: platform super administrator who can see all data.</p>
     */
    default boolean skipDataScope() {
        return false;
    }

    /**
     * Whether to skip tenant filtering entirely.
     * When {@code true}, no WHERE clause is injected for {@code @TenantIdField}.
     *
     * <p>Typical usage: platform super administrator who can see all tenants' data.</p>
     */
    default boolean skipTenantScope() {
        return false;
    }

    default Object resolveUserId() {
        return AuthUtil.getUserId();
    }

    List<Object> resolveDeptIds(DataScopeType scopeType, String module);

    default Object resolveTenantId() {
        return AuthUtil.getTenantId();
    }
}