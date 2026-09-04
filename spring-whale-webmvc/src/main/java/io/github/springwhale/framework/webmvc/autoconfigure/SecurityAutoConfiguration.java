package io.github.springwhale.framework.webmvc.autoconfigure;

import io.github.springwhale.framework.webmvc.security.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
@AutoConfigureAfter(SpringWhaleWebMvcAutoConfiguration.class)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@Slf4j
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                          PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                        AuthenticationProvider authenticationProvider) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.authenticationProvider(authenticationProvider);
        return authBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   SecurityProperties securityProperties,
                                                   List<SecurityConfigProvider> configProviders,
                                                   ObjectProvider<CorsConfigurationSource> corsConfigurationSource) throws Exception {
        List<String> permitAllUrls = new ArrayList<>(securityProperties.getPermitAllUrls());
        configProviders.stream()
                .flatMap(provider -> provider.getPermitAllUrls().stream())
                .forEach(permitAllUrls::add);

        log.info("Permit all URLs: {}", permitAllUrls);

        http
                .csrf(csrf -> {
                    if (!securityProperties.isCsrfEnabled()) {
                        csrf.disable();
                    }
                })
                .cors(cors -> corsConfigurationSource.ifAvailable(cors::configurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> {})
                .authorizeHttpRequests(auth -> {
                    for (String url : permitAllUrls) {
                        auth.requestMatchers(url).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public static SecurityPropertiesValidator securityPropertiesValidator(SecurityProperties securityProperties,
                                                                           Environment environment) {
        return new SecurityPropertiesValidator(securityProperties, environment);
    }

    static class SecurityPropertiesValidator {

        private static final String KNOWN_DEFAULT_SECRET = "SpringWhaleSecretKey2024ForJWTTokenGeneration";
        private static final int MIN_KEY_BYTES = 32;

        private final SecurityProperties securityProperties;
        private final Environment environment;

        SecurityPropertiesValidator(SecurityProperties securityProperties, Environment environment) {
            this.securityProperties = securityProperties;
            this.environment = environment;
        }

        @PostConstruct
        void validate() {
            String secret = securityProperties.getJwtSecret();

            if (!StringUtils.hasText(secret)) {
                throw new IllegalStateException(
                        "JWT secret is not configured. "
                                + "Please set 'spring.whale.web-mvc.security.jwt-secret' in your configuration. "
                                + "The secret must be a strong, unique value of at least 32 bytes (256 bits).");
            }

            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < MIN_KEY_BYTES) {
                throw new IllegalStateException(
                        "JWT secret is too short. Required: at least " + MIN_KEY_BYTES
                                + " bytes (256 bits) for HMAC-SHA256, but got: " + keyBytes.length
                                + " bytes. Please set a stronger 'spring.whale.web-mvc.security.jwt-secret'.");
            }

            if (KNOWN_DEFAULT_SECRET.equals(secret)) {
                throw new IllegalStateException(
                        "The configured JWT secret matches the publicly known default value from the "
                                + "spring-whale source code. This is a critical security risk: anyone with "
                                + "access to the source can forge valid JWT tokens for any user. "
                                + "Please set a unique, strong secret via 'spring.whale.web-mvc.security.jwt-secret'.");
            }

            boolean isProd = false;
            for (String profile : environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                    isProd = true;
                    break;
                }
            }

            log.info("JWT secret validation passed. Key length: {} bytes, production profile: {}",
                    keyBytes.length, isProd);
        }
    }
}