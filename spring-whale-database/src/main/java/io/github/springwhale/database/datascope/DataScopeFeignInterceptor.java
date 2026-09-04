package io.github.springwhale.database.datascope;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Feign request interceptor that transmits the current data scope type,
 * module, and tenant ID to downstream services via HTTP headers.
 *
 * <p>When {@code hmac-secret-key} is configured, all transmitted headers
 * are protected by HMAC-SHA256 signatures with timestamp and nonce for
 * replay protection. The downstream service receives and verifies these
 * signatures via {@link DataScopeServerInterceptor}.</p>
 */
@Slf4j
public class DataScopeFeignInterceptor implements RequestInterceptor {

    private final DataScopeProperties properties;
    private final DataScopeSigner signer;

    public DataScopeFeignInterceptor(DataScopeProperties properties, DataScopeSigner signer) {
        this.properties = properties;
        this.signer = signer;
    }

    @Override
    public void apply(RequestTemplate template) {
        String scopeType = null;
        String module = null;
        String tenantId = null;

        if (properties.isTransmitEnabled()) {
            DataScopeResult scope = DataScopeContext.getScope();
            if (scope != null && scope.getScopeType() != null) {
                scopeType = scope.getScopeType().name();
                module = scope.getModule();
                template.header(properties.getScopeTypeHeader(), scopeType);
                if (module != null && !module.isEmpty()) {
                    template.header(properties.getModuleHeader(), module);
                }
                log.debug("Data scope transmitted via feign: type={}, module={}", scopeType, module);
            }
        }

        if (properties.isTenantEnabled()) {
            Object tid = DataScopeContext.getTenantId();
            if (tid != null) {
                tenantId = tid.toString();
                template.header(properties.getTenantIdHeader(), tenantId);
                log.debug("Tenant id transmitted via feign: {}", tenantId);
            }
        }

        if (signer.isEnabled() && (scopeType != null || tenantId != null)) {
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString();
            String path = template.path();

            String signature = signer.sign(scopeType, module, tenantId, timestamp, nonce, path);

            template.header("X-DataScope-Timestamp", String.valueOf(timestamp));
            template.header("X-DataScope-Nonce", nonce);
            template.header("X-DataScope-Signature", signature);
            log.debug("DataScope HMAC signature added: timestamp={}, nonce={}, path={}",
                    timestamp, nonce, path);
        }
    }
}