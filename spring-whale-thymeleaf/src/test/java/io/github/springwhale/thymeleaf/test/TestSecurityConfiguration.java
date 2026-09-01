package io.github.springwhale.thymeleaf.test;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

@TestConfiguration
public class TestSecurityConfiguration {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin123")
                .authorities("rbac:user:read", "rbac:user:create", "rbac:role:read")
                .build();
        UserDetails superUser = User.withUsername("super")
                .password("{noop}super123")
                .authorities("*")
                .build();
        UserDetails guest = User.withUsername("guest")
                .password("{noop}guest123")
                .authorities("rbac:user:read")
                .build();
        return new InMemoryUserDetailsManager(admin, superUser, guest);
    }

    @Bean
    public SecurityConfigProvider permitAllProvider() {
        return new SecurityConfigProvider() {
            @Override
            public List<String> getPermitAllUrls() {
                return List.of("/**");
            }

            @Override
            public int getOrder() {
                return Integer.MIN_VALUE;
            }
        };
    }
}