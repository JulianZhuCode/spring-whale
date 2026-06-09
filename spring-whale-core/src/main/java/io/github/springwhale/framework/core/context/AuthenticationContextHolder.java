package io.github.springwhale.framework.core.context;

public class AuthenticationContextHolder {

    private static final ThreadLocal<AuthenticationContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static AuthenticationContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void setContext(AuthenticationContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}
