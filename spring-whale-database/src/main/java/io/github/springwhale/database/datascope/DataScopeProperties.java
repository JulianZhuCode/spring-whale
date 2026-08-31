package io.github.springwhale.database.datascope;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
}