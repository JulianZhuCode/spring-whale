package io.github.springwhale.framework.webmvc.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
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
    private final JwtUtil jwtUtil;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.debug("No request attributes available, skipping token transmission");
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String jwt = jwtUtil.extractJwtFromRequest(request);
        if (!StringUtils.hasText(jwt)) {
            log.debug("No JWT found in current request, skipping token transmission");
            return;
        }

        String headerValue = securityProperties.getTokenPrefix() + jwt;
        template.header(securityProperties.getTokenHeader(), headerValue);

        log.debug("JWT token transmitted via Feign: header={}, prefix={}",
                securityProperties.getTokenHeader(), securityProperties.getTokenPrefix());
    }
}