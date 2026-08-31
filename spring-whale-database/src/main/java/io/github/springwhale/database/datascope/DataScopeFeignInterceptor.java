package io.github.springwhale.database.datascope;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign request interceptor that transmits the current data scope type,
 * module, and tenant ID to downstream services via HTTP headers.
 *
 * <p>The downstream service receives these headers via
 * {@link DataScopeServerInterceptor}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class DataScopeFeignInterceptor implements RequestInterceptor {

    private final DataScopeProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        if (properties.isTransmitEnabled()) {
            transmitDataScope(template);
        }
        if (properties.isTenantEnabled()) {
            transmitTenantId(template);
        }
    }

    private void transmitDataScope(RequestTemplate template) {
        DataScopeResult scope = DataScopeContext.getScope();
        if (scope == null || scope.getScopeType() == null) {
            return;
        }

        template.header(properties.getScopeTypeHeader(), scope.getScopeType().name());

        if (scope.getModule() != null && !scope.getModule().isEmpty()) {
            template.header(properties.getModuleHeader(), scope.getModule());
        }

        log.debug("Data scope transmitted via feign: type={}, module={}",
                scope.getScopeType(), scope.getModule());
    }

    private void transmitTenantId(RequestTemplate template) {
        Object tenantId = DataScopeContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        template.header(properties.getTenantIdHeader(), tenantId.toString());
        log.debug("Tenant id transmitted via feign: {}", tenantId);
    }
}