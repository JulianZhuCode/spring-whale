package io.github.springwhale.framework.webmvc.autoconfigure;

import io.github.springwhale.framework.webmvc.security.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Comparator;
import java.util.List;

/**
 * Auto-configuration for Spring Security with JWT-based stateless authentication.
 *
 * <h3>What this provides</h3>
 * <ul>
 *   <li>Stateless session management (no server-side sessions)</li>
 *   <li>JWT extraction via {@link JwtAuthenticationFilter} (header or cookie)</li>
 *   <li>BCrypt password encoding</li>
 *   <li>DAO-based authentication against the configured {@link UserDetailsService}</li>
 *   <li>Admin console support: unauthenticated browser requests to {@code /admin/**}
 *       are redirected to the login page; REST API requests receive 401</li>
 *   <li>SPI-based extension via {@link SecurityConfigProvider} for downstream modules
 *       to declare permit-all URLs and custom {@link HttpSecurity} configuration</li>
 *   <li>Feign interceptor ({@link SecurityFeignInterceptor}) for propagating JWT
 *       tokens across service-to-service calls</li>
 * </ul>
 *
 * <h3>Configuration</h3>
 * All security settings are under {@code spring.whale.web-mvc.security.*}
 * via {@link SecurityProperties}.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SecurityAutoConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;
    private final List<SecurityConfigProvider> configProviders;

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider());
        return authBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<CorsConfigurationSource> corsConfigurationSource) throws Exception {
        // Collect all URLs that permit anonymous access
        List<String> permitAllUrls = collectPermitAllUrls();

        log.info("Permit all URLs: {}", permitAllUrls);

        http
                .csrf(csrf -> {
                    if (!securityProperties.isCsrfEnabled()) {
                        csrf.disable();
                    }
                })
                .cors(cors -> corsConfigurationSource.ifAvailable(cors::configurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(adminConsoleEntryPoint())
                )
                .authorizeHttpRequests(auth -> {
                    for (String url : permitAllUrls) {
                        auth.requestMatchers(url).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        applyCustomConfigurations(http);

        return http.build();
    }

    /**
     * Entry point that redirects unauthenticated browser requests under
     * {@code /admin/**} to the login page, while REST API requests still
     * receive a 401 status.
     * <p>
     * Avoids redirect loops by not redirecting the login page itself.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEntryPoint adminConsoleEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) -> {
            String path = request.getRequestURI();
            if (path.startsWith("/admin") && !path.equals("/admin/login")) {
                // Add reason parameter so the login page can show diagnostic info
                String reason;
                // Check if there was a cookie but validation failed
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                boolean hasSwToken = false;
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie c : cookies) {
                        if (securityProperties.getTokenCookieName().equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                            hasSwToken = true;
                            break;
                        }
                    }
                }
                reason = hasSwToken ? "token_invalid" : "no_token";
                log.warn("Auth entry point: path={}, reason={}", path, reason);
                String redirectUrl = "/admin/login?redirect=" +
                        java.net.URLEncoder.encode(path, java.nio.charset.StandardCharsets.UTF_8) +
                        "&reason=" + reason;
                response.sendRedirect(redirectUrl);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            }
        };
    }

    private List<String> collectPermitAllUrls() {
        return configProviders.stream()
                .sorted(Comparator.comparingInt(SecurityConfigProvider::getOrder))
                .flatMap(provider -> provider.getPermitAllUrls().stream())
                .toList();
    }

    private void applyCustomConfigurations(HttpSecurity http) {
        configProviders.stream()
                .sorted(Comparator.comparingInt(SecurityConfigProvider::getOrder))
                .forEach(provider -> {
                    try {
                        provider.configure(http);
                    } catch (Exception e) {
                        log.error("Failed to apply security configuration from provider: {}",
                                provider.getClass().getName(), e);
                    }
                });
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(feign.RequestInterceptor.class)
    public SecurityFeignInterceptor securityFeignInterceptor(SecurityProperties props, JwtUtil jwtUtil) {
        return new SecurityFeignInterceptor(props, jwtUtil);
    }
}