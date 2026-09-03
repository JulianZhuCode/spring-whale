package io.github.springwhale.platform.task;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

/**
 * Test security configuration: permit all requests so REST endpoints can be
 * exercised without JWT, plus an in-memory user required by the JWT filter.
 */
@TestConfiguration
public class TestSecurityConfiguration {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

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
