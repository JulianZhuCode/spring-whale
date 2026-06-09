package io.github.springwhale.framework.webmvc.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public interface SecurityConfigProvider {

    default List<String> getPermitAllUrls() {
        return List.of();
    }

    default void configure(HttpSecurity http) throws Exception {
        // 默认不做任何配置
    }

    default List<RequestMatcher> getCustomRequestMatchers() {
        return List.of();
    }

    default int getOrder() {
        return 0;
    }
}
