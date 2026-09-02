package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import io.github.springwhale.platform.rbac.dto.request.LoginRequest;
import io.github.springwhale.platform.rbac.dto.response.LoginResponse;
import io.github.springwhale.platform.rbac.service.AuthService;
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
    @AdviceIgnore
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}