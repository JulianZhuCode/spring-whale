package io.github.springwhale.framework.thymeleaf.autoconfigure;

import io.github.springwhale.framework.thymeleaf.config.AdminProperties;
import io.github.springwhale.framework.thymeleaf.controller.AdminConsoleController;
import io.github.springwhale.framework.thymeleaf.controller.AdminControllerAdvice;
import io.github.springwhale.framework.thymeleaf.controller.AdminErrorController;
import io.github.springwhale.framework.thymeleaf.controller.AdminLoginController;
import io.github.springwhale.framework.thymeleaf.menu.AdminMenuProvider;
import io.github.springwhale.framework.thymeleaf.security.ThymeleafSecurityConfigProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Auto-configuration for Spring Whale Thymeleaf admin console.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning.
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(AdminProperties.class)
public class SpringWhaleThymeleafAutoConfiguration {

    private static final String ADMIN_PATH = "/admin";
    private static final String ADMIN_LOGIN_PATH = "/admin/login";
    private static final String TOKEN_COOKIE = "sw_token";

    @Bean
    @ConditionalOnMissingBean
    public ThymeleafSecurityConfigProvider thymeleafSecurityConfigProvider() {
        return new ThymeleafSecurityConfigProvider();
    }

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
                String redirectUrl = ADMIN_LOGIN_PATH + "?redirect=" +
                        URLEncoder.encode(path, StandardCharsets.UTF_8) +
                        "&reason=" + reason;
                response.sendRedirect(redirectUrl);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminLoginController adminLoginController(AdminProperties adminProperties) {
        return new AdminLoginController(adminProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminErrorController adminErrorController(AdminProperties adminProperties, MessageSource messageSource) {
        return new AdminErrorController(adminProperties, messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminConsoleController adminConsoleController() {
        return new AdminConsoleController();
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminControllerAdvice adminControllerAdvice(
            List<AdminMenuProvider> menuProviders,
            AdminProperties adminProperties) {
        return new AdminControllerAdvice(menuProviders, adminProperties);
    }
}