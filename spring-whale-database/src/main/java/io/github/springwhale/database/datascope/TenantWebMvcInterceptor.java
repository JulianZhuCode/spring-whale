package io.github.springwhale.database.datascope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring MVC interceptor that detects {@code @NonTenant}-annotated controller
 * methods and sets a flag in {@link DataScopeContext} to skip tenant filtering.
 *
 * <p>The flag is cleared in {@link #afterCompletion} to restore tenant filtering
 * for subsequent requests.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class TenantWebMvcInterceptor implements HandlerInterceptor {

    private static final String SKIP_FLAG = TenantWebMvcInterceptor.class.getName() + ".SKIP";
    private final DataScopeProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isTenantEnabled()) {
            return true;
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            NonTenant nonTenant = handlerMethod.getMethodAnnotation(NonTenant.class);
            if (nonTenant != null) {
                DataScopeContext.setSkipTenant(true);
                request.setAttribute(SKIP_FLAG, Boolean.TRUE);
                log.debug("@NonTenant detected, tenant filter skipped for: {}",
                        handlerMethod.getShortLogMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (Boolean.TRUE.equals(request.getAttribute(SKIP_FLAG))) {
            DataScopeContext.setSkipTenant(false);
        }
    }
}