package io.github.springwhale.test;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

/**
 * Test configuration that provides the required Security beans
 * for unit tests in the webmvc module.
 */
@TestConfiguration
public class TestSecurityConfiguration {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("test")
                .password("{noop}test")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Permit all URLs in test environment so that existing tests
     * can access endpoints without authentication.
     */
    @Bean
    public SecurityConfigProvider permitAllProvider() {
        return new SecurityConfigProvider() {
            @Override
            public List<String> getPermitAllUrls() {
                return List.of("/**");
            }
        };
    }
}
