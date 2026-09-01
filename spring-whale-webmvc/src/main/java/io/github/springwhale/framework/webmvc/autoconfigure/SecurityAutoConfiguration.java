package io.github.springwhale.framework.webmvc.autoconfigure;

import io.github.springwhale.framework.webmvc.security.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

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
}