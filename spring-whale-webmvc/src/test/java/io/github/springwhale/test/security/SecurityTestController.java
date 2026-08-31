package io.github.springwhale.test.security;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SecurityTestController {

    @GetMapping("/api/secure")
    public Map<String, Object> apiSecure() {
        return buildAuthInfo();
    }

    @GetMapping("/admin/secure")
    public Map<String, Object> adminSecure() {
        return buildAuthInfo();
    }

    @GetMapping("/api/public")
    public Map<String, Object> apiPublic() {
        return buildAuthInfo();
    }

    private Map<String, Object> buildAuthInfo() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var ctx = AuthenticationContextHolder.getContext();
        Map<String, Object> result = new HashMap<>();
        result.put("authenticated", auth != null && auth.isAuthenticated());
        result.put("username", auth != null ? auth.getName() : null);
        result.put("ctxUserId", ctx != null ? ctx.getUserId() : null);
        result.put("ctxUsername", ctx != null ? ctx.getUsername() : null);
        return result;
    }
}