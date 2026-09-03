package io.github.springwhale.framework.core.utils;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;

public class AuthUtil {

    public static Long getUserId() {
        var context = AuthenticationContextHolder.getContext();
        return context != null ? context.getUserId() : null;
    }

    public static String getUsername() {
        var context = AuthenticationContextHolder.getContext();
        return context != null ? context.getUsername() : null;
    }

    public static Long getTenantId() {
        var context = AuthenticationContextHolder.getContext();
        return context != null ? context.getTenantId() : null;
    }
}