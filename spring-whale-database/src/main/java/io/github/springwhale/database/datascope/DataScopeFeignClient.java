package io.github.springwhale.database.datascope;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign client for the RBAC data scope remote API.
 *
 * <p>Activated when {@code spring.whale.database.datascope.remote-rbac-url} is configured.
 * The URL from the configuration is used as the target base URL.</p>
 */
@FeignClient(name = "rbac-data-scope", url = "${spring.whale.database.datascope.remote-rbac-url}")
public interface DataScopeFeignClient extends DataScopeRemoteApi {
}