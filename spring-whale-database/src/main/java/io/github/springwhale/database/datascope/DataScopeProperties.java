package io.github.springwhale.database.datascope;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for data scope and tenant isolation.
 *
 * <p>Prefix: {@code spring.whale.database.datascope}</p>
 *
 * <h3>Complete configuration example</h3>
 * <pre>{@code
 * spring.whale.database.datascope:
 *   enabled: true
 *   transmit-enabled: true
 *   scope-type-header: X-DataScope-Type
 *   module-header: X-DataScope-Module
 *   tenant-enabled: true
 *   tenant-id-header: X-Tenant-Id
 *   cache:
 *     skip-ttl: 5m
 *     dept-ttl: 2m
 *     fallback-ttl: 30m
 *     null-ttl: 30s
 * }</pre>
 */
@Data
@ConfigurationProperties(prefix = "spring.whale.database.datascope")
public class DataScopeProperties {

    private boolean enabled = true;

    private boolean transmitEnabled = true;

    private String scopeTypeHeader = "X-DataScope-Type";

    private String moduleHeader = "X-DataScope-Module";

    private boolean tenantEnabled = true;

    private String tenantIdHeader = "X-Tenant-Id";

    /**
     * Base URL of the remote RBAC service for data scope resolution.
     * When configured, {@code SmartDataScopeHandler} is activated to call the RBAC
     * REST API remotely instead of using local JPA repositories.
     *
     * <p>Example: {@code http://rbac-service:8080}</p>
     */
    private String remoteRbacUrl;

    /**
     * Whether to expose the remote data scope resolution API endpoints
     * ({@code /api/rbac/datascope/skip/{userId}} and
     * {@code /api/rbac/datascope/resolve/{userId}}).
     *
     * <p>Should only be enabled in the RBAC service. Defaults to {@code false}
     * for security: downstream services should never expose these endpoints.</p>
     */
    private boolean exposeRemoteApi = false;

    /**
     * Cache TTL configuration for data scope queries.
     *
     * <p>Shorter TTLs mean fresher permissions but higher load on the RBAC service.
     * Adjust based on your permission change frequency and consistency requirements.</p>
     */
    private Cache cache = new Cache();

    @Data
    public static class Cache {

        /**
         * TTL for {@code skipDataScope()} results.
         * Super admin status changes are rare; 5 minutes is acceptable for most cases.
         */
        private Duration skipTtl = Duration.ofMinutes(5);

        /**
         * TTL for {@code resolveDeptIds()} results.
         * Department scope changes are more frequent; keep this short.
         */
        private Duration deptTtl = Duration.ofMinutes(2);

        /**
         * TTL for fallback cache entries used when the RBAC service is unreachable.
         * Must be significantly longer than the primary TTLs.
         */
        private Duration fallbackTtl = Duration.ofMinutes(30);

        /**
         * TTL for null-value placeholders (cache penetration protection).
         */
        private Duration nullTtl = Duration.ofSeconds(30);
    }
}