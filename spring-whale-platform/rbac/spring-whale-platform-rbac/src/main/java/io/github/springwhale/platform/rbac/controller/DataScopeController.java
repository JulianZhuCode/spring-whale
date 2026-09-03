package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.datascope.DataScopeRemoteApi;
import io.github.springwhale.database.datascope.DataScopeResolveResponse;
import io.github.springwhale.database.datascope.DataScopeSkipResponse;
import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Internal REST API for remote data scope resolution.
 *
 * <p>Implements {@link DataScopeRemoteApi} to ensure the contract matches
 * the {@code DataScopeFeignClient} used by downstream services.</p>
 *
 * <h3>Security</h3>
 * <p>This endpoint is <b>disabled by default</b>. It must be explicitly enabled
 * via {@code spring.whale.database.datascope.expose-remote-api=true} only in the
 * RBAC service. In production, this endpoint should be additionally protected
 * at the gateway level (e.g., only allow internal service-to-service traffic).</p>
 *
 * <pre>{@code
 * spring.whale.database.datascope:
 *   expose-remote-api: true  # Enable remote scope resolution API
 * }</pre>
 */
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "expose-remote-api", havingValue = "true")
public class DataScopeController implements DataScopeRemoteApi {

    private final RBACDataScopeHandler handler;

    @Override
    @GetMapping("/api/rbac/datascope/skip/{userId}")
    public DataScopeSkipResponse skipDataScope(@PathVariable Integer userId) {
        return new DataScopeSkipResponse(handler.skipDataScope(userId));
    }

    @Override
    @GetMapping("/api/rbac/datascope/skip-tenant/{userId}")
    public DataScopeSkipResponse skipTenantScope(@PathVariable Integer userId) {
        return new DataScopeSkipResponse(handler.skipTenantScope(userId));
    }

    @Override
    @GetMapping("/api/rbac/datascope/resolve/{userId}")
    public DataScopeResolveResponse resolveDeptIds(@PathVariable Integer userId,
                                                   @RequestParam DataScopeType scopeType,
                                                   @RequestParam(required = false) String module) {
        List<Object> deptIds = handler.resolveDeptIds(userId, scopeType, module);
        return new DataScopeResolveResponse(deptIds);
    }

    /**
     * Evict all cached data scope entries for the given user.
     * Call this after role/permission changes to force fresh resolution
     * on the next request.
     */
    @DeleteMapping("/api/rbac/datascope/cache/{userId}")
    public void evictCache(@PathVariable Integer userId) {
        handler.evictUser(userId);
    }
}