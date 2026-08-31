package io.github.springwhale.database.datascope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that receives data scope and tenant ID from HTTP headers
 * and populates {@link DataScopeContext}.
 *
 * <p>Entry point for cross-service scope/tenant propagation in microservice
 * architectures. The Feign counterpart is {@link DataScopeFeignInterceptor}.</p>
 *
 * <p>{@link #afterCompletion} unconditionally calls {@link DataScopeContext#clear()}
 * to prevent ThreadLocal leaks, regardless of whether the handler threw an exception.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class DataScopeServerInterceptor implements HandlerInterceptor {

    private final DataScopeProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (properties.isTransmitEnabled()) {
            receiveDataScope(request);
        }
        if (properties.isTenantEnabled()) {
            receiveTenantId(request);
        }
        return true;
    }

    private void receiveDataScope(HttpServletRequest request) {
        String scopeTypeStr = request.getHeader(properties.getScopeTypeHeader());
        if (scopeTypeStr == null || scopeTypeStr.isEmpty()) {
            return;
        }

        try {
            DataScopeType scopeType = DataScopeType.valueOf(scopeTypeStr);
            String module = request.getHeader(properties.getModuleHeader());

            DataScopeResult result = new DataScopeResult();
            result.setScopeType(scopeType);
            result.setModule(module);

            DataScopeContext.pushScope(result);
            log.debug("Data scope transmitted from header: type={}, module={}", scopeType, module);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid DataScopeType from header: {}", scopeTypeStr);
        }
    }

    private void receiveTenantId(HttpServletRequest request) {
        String tenantIdStr = request.getHeader(properties.getTenantIdHeader());
        if (tenantIdStr == null || tenantIdStr.isEmpty()) {
            return;
        }
        try {
            Object tenantId = parseTenantId(tenantIdStr);
            DataScopeContext.setTenantId(tenantId);
            log.debug("Tenant id transmitted from header: {}", tenantId);
        } catch (NumberFormatException e) {
            log.warn("Invalid tenant id from header: {}", tenantIdStr);
        }
    }

    private Object parseTenantId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        DataScopeContext.clear();
    }
}