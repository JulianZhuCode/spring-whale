package io.github.springwhale.database.datascope;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Remote API contract for data scope resolution.
 *
 * <p>This interface defines the REST contract and is dual-purpose:
 * <ul>
 *   <li>Implemented by {@code DataScopeController} in the RBAC module</li>
 *   <li>Used as a {@link org.springframework.cloud.openfeign.FeignClient} template
 *       by {@link DataScopeFeignClient} in downstream services</li>
 * </ul>
 */
public interface DataScopeRemoteApi {

    @GetMapping("/api/rbac/datascope/skip/{userId}")
    DataScopeSkipResponse skipDataScope(@PathVariable Long userId);

    @GetMapping("/api/rbac/datascope/skip-tenant/{userId}")
    DataScopeSkipResponse skipTenantScope(@PathVariable Long userId);

    @GetMapping("/api/rbac/datascope/resolve/{userId}")
    DataScopeResolveResponse resolveDeptIds(@PathVariable Long userId,
                                            @RequestParam DataScopeType scopeType,
                                            @RequestParam(required = false) String module);
}