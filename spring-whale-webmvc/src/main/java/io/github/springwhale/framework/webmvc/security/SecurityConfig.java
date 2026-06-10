package io.github.springwhale.framework.webmvc.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Comparator;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;
    private final List<io.github.springwhale.framework.webmvc.security.SecurityConfigProvider> configProviders;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider());
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        // Collect all URLs that permit anonymous access
        List<String> permitAllUrls = collectPermitAllUrls();

        log.info("Permit all URLs: {}", permitAllUrls);

        http
                .csrf(csrf -> {
                    if (!securityProperties.isCsrfEnabled()) {
                        csrf.disable();
                    }
                })
                .cors(cors -> {
                    if (securityProperties.isCorsEnabled()) {
                        cors.configurationSource(corsConfigurationSource);
                    }
                })
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
    public AuthenticationEntryPoint adminConsoleEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response,
                AuthenticationException authException) -> {
            String path = request.getRequestURI();
            if (path.startsWith("/admin") && !path.equals("/admin/login")) {
                // Add reason parameter so the login page can show diagnostic info
                String reason = "auth_required";
                // Check if there was a cookie but validation failed
                jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                boolean hasSwToken = false;
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie c : cookies) {
                        if ("sw_token".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                            hasSwToken = true;
                            break;
                        }
                    }
                }
                if (hasSwToken) {
                    reason = "token_invalid";
                } else {
                    reason = "no_token";
                }
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
                .sorted(Comparator.comparingInt(io.github.springwhale.framework.webmvc.security.SecurityConfigProvider::getOrder))
                .flatMap(provider -> provider.getPermitAllUrls().stream())
                .toList();
    }

    private void applyCustomConfigurations(HttpSecurity http) {
        configProviders.stream()
                .sorted(Comparator.comparingInt(io.github.springwhale.framework.webmvc.security.SecurityConfigProvider::getOrder))
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        securityProperties.getAllowedOriginPatterns().forEach(configuration::addAllowedOriginPattern);
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
