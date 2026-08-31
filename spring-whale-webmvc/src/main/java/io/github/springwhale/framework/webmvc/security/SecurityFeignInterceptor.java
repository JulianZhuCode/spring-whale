package io.github.springwhale.framework.webmvc.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RequiredArgsConstructor
public class SecurityFeignInterceptor implements RequestInterceptor {

    private final SecurityProperties securityProperties;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.debug("No request attributes available, skipping token transmission");
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String jwt = extractJwt(request);
        if (!StringUtils.hasText(jwt)) {
            log.debug("No JWT found in current request, skipping token transmission");
            return;
        }

        String headerValue = securityProperties.getTokenPrefix() + jwt;
        template.header(securityProperties.getTokenHeader(), headerValue);

        log.debug("JWT token transmitted via Feign: header={}, prefix={}",
                securityProperties.getTokenHeader(), securityProperties.getTokenPrefix());
    }

    /**
     * Extract JWT from request, checking both the Authorization header
     * (for REST API clients) and the "sw_token" cookie (for admin console).
     */
    private String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sw_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}