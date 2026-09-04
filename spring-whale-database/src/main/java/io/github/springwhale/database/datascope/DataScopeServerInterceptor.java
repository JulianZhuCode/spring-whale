package io.github.springwhale.database.datascope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that receives data scope and tenant ID from HTTP headers
 * and populates {@link DataScopeContext}.
 *
 * <p>Entry point for cross-service scope/tenant propagation in microservice
 * architectures. The Feign counterpart is {@link DataScopeFeignInterceptor}.</p>
 *
 * <p>When {@code hmac-secret-key} is configured, HMAC-SHA256 signature
 * verification is enforced with timestamp and nonce validation to prevent
 * header forgery and replay attacks.</p>
 *
 * <p>{@link #afterCompletion} unconditionally calls {@link DataScopeContext#clear()}
 * to prevent ThreadLocal leaks, regardless of whether the handler threw an exception.</p>
 */
@Slf4j
public class DataScopeServerInterceptor implements HandlerInterceptor {

    private final DataScopeProperties properties;
    private final DataScopeSigner signer;

    public DataScopeServerInterceptor(DataScopeProperties properties, DataScopeSigner signer) {
        this.properties = properties;
        this.signer = signer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String scopeType = null;
        String module = null;
        String tenantId = null;

        if (properties.isTransmitEnabled()) {
            scopeType = request.getHeader(properties.getScopeTypeHeader());
            module = request.getHeader(properties.getModuleHeader());
        }
        if (properties.isTenantEnabled()) {
            tenantId = request.getHeader(properties.getTenantIdHeader());
        }

        boolean hasDataScopeHeaders = (scopeType != null && !scopeType.isEmpty())
                || (tenantId != null && !tenantId.isEmpty());

        if (!hasDataScopeHeaders) {
            return true;
        }

        if (signer.isEnabled()) {
            String signature = request.getHeader("X-DataScope-Signature");
            String timestampStr = request.getHeader("X-DataScope-Timestamp");
            String nonce = request.getHeader("X-DataScope-Nonce");

            if (timestampStr == null || timestampStr.isBlank()) {
                log.warn("DataScope HMAC verification failed: missing X-DataScope-Timestamp header");
                response.setStatus(403);
                return false;
            }

            long timestamp;
            try {
                timestamp = Long.parseLong(timestampStr);
            } catch (NumberFormatException e) {
                log.warn("DataScope HMAC verification failed: invalid timestamp {}", timestampStr);
                response.setStatus(403);
                return false;
            }

            String path = request.getRequestURI();
            if (!signer.verify(signature, scopeType, module, tenantId, timestamp, nonce, path)) {
                log.warn("DataScope HMAC verification failed for path={} from remote={}",
                        path, request.getRemoteAddr());
                response.setStatus(403);
                return false;
            }
        } else {
            log.debug("DataScope HMAC signing disabled, accepting plaintext headers");
        }

        if (scopeType != null && !scopeType.isEmpty()) {
            receiveDataScope(scopeType, module);
        }
        if (tenantId != null && !tenantId.isEmpty()) {
            receiveTenantId(tenantId);
        }
        return true;
    }

    private void receiveDataScope(String scopeTypeStr, String module) {
        try {
            DataScopeType scopeType = DataScopeType.valueOf(scopeTypeStr);
            DataScopeResult result = new DataScopeResult();
            result.setScopeType(scopeType);
            result.setModule(module);
            DataScopeContext.pushScope(result);
            log.debug("Data scope received from header: type={}, module={}", scopeType, module);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid DataScopeType from header: {}", scopeTypeStr);
        }
    }

    private void receiveTenantId(String tenantIdStr) {
        try {
            Object tenantId = parseTenantId(tenantIdStr);
            DataScopeContext.setTenantId(tenantId);
            log.debug("Tenant id received from header: {}", tenantId);
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