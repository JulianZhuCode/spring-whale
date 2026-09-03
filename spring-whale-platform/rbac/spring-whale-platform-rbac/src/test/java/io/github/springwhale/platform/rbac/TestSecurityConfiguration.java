package io.github.springwhale.platform.rbac;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

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

    @Bean
    @Primary
    public EventPublisher testEventPublisher(EventProperties properties,
                                              ObjectMapper jsonMapper,
                                              ApplicationEventPublisher applicationEventPublisher) {
        return new EventPublisher(properties, jsonMapper, null) {
            @Override
            protected void doSend(EventMessage message, String partitionKey) {
                applicationEventPublisher.publishEvent(message);
            }

            @Override
            public void publishAfterCommit(Object event) {
                publish(event);
            }
        };
    }
}