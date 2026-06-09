package io.github.springwhale.rbac.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Comparator;
import java.util.List;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final SecurityProperties securityProperties;
    private final List<SecurityConfigProvider> configProviders;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        // 收集所有允许匿名访问的 URL
        List<String> permitAllUrls = collectPermitAllUrls();

        log.info("Permit all URLs: {}", permitAllUrls);

        http
                // CSRF 配置
                .csrf(csrf -> {
                    if (!securityProperties.isCsrfEnabled()) {
                        csrf.disable();
                    }
                })

                // CORS 配置
                .cors(cors -> {
                    if (securityProperties.isCorsEnabled()) {
                        cors.configurationSource(corsConfigurationSource);
                    }
                })

                // 设置会话策略为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 授权规则
                .authorizeHttpRequests(auth -> {
                    // 配置的匿名访问接口
                    for (String url : permitAllUrls) {
                        auth.requestMatchers(url).permitAll();
                    }

                    // 其他请求需要认证
                    auth.anyRequest().authenticated();
                })

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 应用提供者的自定义配置
        applyCustomConfigurations(http);

        return http.build();
    }

    /**
     * 收集所有允许匿名访问的 URL
     */
    private List<String> collectPermitAllUrls() {
        return configProviders.stream()
                .sorted(Comparator.comparingInt(SecurityConfigProvider::getOrder))
                .flatMap(provider -> provider.getPermitAllUrls().stream())
                .toList();
    }

    /**
     * 应用提供者的自定义配置
     */
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

    /**
     * CORS 配置源
     */
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
