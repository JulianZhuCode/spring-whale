package io.github.springwhale.framework.webmvc.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import java.util.List;

/**
 * SPI for downstream modules to contribute security configuration.
 *
 * <p>Implementations are auto-detected and applied in {@link #getOrder()}
 * ascending order during {@link SecurityAutoConfiguration}.</p>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * @Component
 * public class MySecurityConfig implements SecurityConfigProvider {
 *     public List<String> getPermitAllUrls() { return List.of("/public/**"); }
 *     public void configure(HttpSecurity http) { http.headers(h -> h.frameOptions(f -> f.sameOrigin())); }
 *     public int getOrder() { return 100; }
 * }
 * }</pre>
 */
public interface SecurityConfigProvider {

    /**
     * URLs that should bypass authentication.
     * Each entry is passed directly to {@code HttpSecurity.authorizeHttpRequests().requestMatchers()}.
     */
    default List<String> getPermitAllUrls() {
        return List.of();
    }

    /**
     * Hook for custom {@link HttpSecurity} configuration beyond URL authorization.
     */
    default void configure(HttpSecurity http) throws Exception {
    }

    /**
     * Execution order — lower values execute first.
     * Providers with the same order are applied in an undefined sequence.
     */
    default int getOrder() {
        return 0;
    }
}