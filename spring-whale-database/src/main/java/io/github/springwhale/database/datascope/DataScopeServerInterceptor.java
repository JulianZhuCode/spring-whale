package io.github.springwhale.database.datascope;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class DataScopeServerInterceptor implements HandlerInterceptor {

    private static final String PUSHED_FLAG = DataScopeServerInterceptor.class.getName() + ".PUSHED";
    private final DataScopeProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isTransmitEnabled()) {
            return true;
        }

        String scopeTypeStr = request.getHeader(properties.getScopeTypeHeader());
        if (scopeTypeStr == null || scopeTypeStr.isEmpty()) {
            return true;
        }

        try {
            DataScopeType scopeType = DataScopeType.valueOf(scopeTypeStr);
            String module = request.getHeader(properties.getModuleHeader());

            DataScopeResult result = new DataScopeResult();
            result.setScopeType(scopeType);
            result.setModule(module);

            DataScopeContext.pushScope(result);
            request.setAttribute(PUSHED_FLAG, Boolean.TRUE);
            log.debug("Data scope transmitted from header: type={}, module={}", scopeType, module);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid DataScopeType from header: {}", scopeTypeStr);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (Boolean.TRUE.equals(request.getAttribute(PUSHED_FLAG))) {
            DataScopeContext.clear();
        }
    }
}