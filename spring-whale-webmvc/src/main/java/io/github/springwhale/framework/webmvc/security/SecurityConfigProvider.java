package io.github.springwhale.framework.webmvc.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public interface SecurityConfigProvider {

    default List<String> getPermitAllUrls() {
        return List.of();
    }

    default void configure(HttpSecurity http) throws Exception {
        // Default: no custom configuration
    }

    default List<RequestMatcher> getCustomRequestMatchers() {
        return List.of();
    }

    default int getOrder() {
        return 0;
    }
}
