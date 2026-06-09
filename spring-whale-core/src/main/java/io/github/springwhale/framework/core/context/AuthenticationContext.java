package io.github.springwhale.framework.core.context;

public interface AuthenticationContext {

    Integer userId();

    String username();

    boolean isAuthenticated();
}
