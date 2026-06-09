package io.github.springwhale.framework.core.utils;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;

public class AuthUtil {

    public static Integer getUserId() {
        var context = AuthenticationContextHolder.getContext();
        return context != null ? context.userId() : null;
    }

    public static String getUsername() {
        var context = AuthenticationContextHolder.getContext();
        return context != null ? context.username() : null;
    }

    public static boolean isAuthenticated() {
        var context = AuthenticationContextHolder.getContext();
        return context != null && context.isAuthenticated();
    }
}
