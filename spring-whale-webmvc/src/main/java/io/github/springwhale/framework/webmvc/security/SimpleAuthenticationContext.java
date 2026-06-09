package io.github.springwhale.framework.webmvc.security;

import io.github.springwhale.framework.core.context.AuthenticationContext;

public record SimpleAuthenticationContext(Integer userId, String username) implements AuthenticationContext {

    @Override
    public boolean isAuthenticated() {
        return userId != null && username != null;
    }
}
