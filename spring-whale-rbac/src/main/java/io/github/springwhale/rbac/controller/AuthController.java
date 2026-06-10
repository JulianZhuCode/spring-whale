package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.framework.core.utils.AuthUtil;
import io.github.springwhale.rbac.dto.request.ChangePasswordRequest;
import io.github.springwhale.rbac.dto.request.LoginRequest;
import io.github.springwhale.rbac.dto.request.RegisterRequest;
import io.github.springwhale.rbac.dto.response.LoginResponse;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/api/rbac/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     * POST /api/rbac/auth/login
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * User registration
     * POST /api/rbac/auth/register
     */
    @PostMapping("/register")
    public UserVO register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Get current user info
     * GET /api/rbac/auth/me
     */
    @GetMapping("/me")
    public String getCurrentUser() {
        Integer userId = AuthUtil.getUserId();
        String username = AuthUtil.getUsername();
        return "Current user: " + username + " (ID: " + userId + ")";
    }

    /**
     * Change password
     * POST /api/rbac/auth/change-password
     */
    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Integer userId = AuthUtil.getUserId();
        if (userId == null) {
            throw BusinessException.create("USER_NOT_AUTHENTICATED", "User not authenticated");
        }

        authService.changePassword(userId, request);
    }

    /**
     * Logout (frontend removes token)
     * POST /api/rbac/auth/logout
     */
    @PostMapping("/logout")
    public String logout() {
        return "Logout successful";
    }
}
