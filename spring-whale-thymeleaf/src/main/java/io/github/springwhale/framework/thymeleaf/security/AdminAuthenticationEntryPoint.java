package io.github.springwhale.framework.thymeleaf.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Authentication entry point for the admin console.
 * <p>
 * Redirects unauthenticated browser requests under {@code /admin/**} to the
 * login page instead of returning a 401 status. This provides a better UX
 * for browser-based admin consoles while REST API requests still receive 401.
 * </p>
 */
@Slf4j
@Configuration
public class AdminAuthenticationEntryPoint {

    private static final String ADMIN_PATH = "/admin";
    private static final String ADMIN_LOGIN_PATH = "/admin/login";
    private static final String TOKEN_COOKIE = "sw_token";

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEntryPoint adminConsoleEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) -> {
            String path = request.getRequestURI();
            if (path.startsWith(ADMIN_PATH) && !path.equals(ADMIN_LOGIN_PATH)) {
                String reason;
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                boolean hasSwToken = false;
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie c : cookies) {
                        if (TOKEN_COOKIE.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                            hasSwToken = true;
                            break;
                        }
                    }
                }
                reason = hasSwToken ? "token_invalid" : "no_token";
                log.warn("Auth entry point: path={}, reason={}", path, reason);
                String redirectUrl = ADMIN_LOGIN_PATH + "?redirect=" +
                        URLEncoder.encode(path, StandardCharsets.UTF_8) +
                        "&reason=" + reason;
                response.sendRedirect(redirectUrl);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        };
    }
}