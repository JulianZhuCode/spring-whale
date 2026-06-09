package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.utils.AuthUtil;
import io.github.springwhale.rbac.dto.request.ChangePasswordRequest;
import io.github.springwhale.rbac.dto.request.LoginRequest;
import io.github.springwhale.rbac.dto.request.RegisterRequest;
import io.github.springwhale.rbac.dto.response.AuthResponse;
import io.github.springwhale.rbac.dto.response.LoginResponse;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/rbac/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/rbac/auth/login
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 用户注册
     * POST /api/rbac/auth/register
     */
    @PostMapping("/register")
    public UserVO register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 获取当前用户信息
     * GET /api/rbac/auth/me
     */
    @GetMapping("/me")
    public AuthResponse getCurrentUser() {
        Integer userId = AuthUtil.getUserId();
        String username = AuthUtil.getUsername();

        return new AuthResponse("当前用户: " + username + " (ID: " + userId + ")");
    }

    /**
     * 修改密码
     * POST /api/rbac/auth/change-password
     */
    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Integer userId = AuthUtil.getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户未登录");
        }
            
        authService.changePassword(userId, request);
    }

    /**
     * 退出登录（前端删除 Token 即可）
     * POST /api/rbac/auth/logout
     */
    @PostMapping("/logout")
    public AuthResponse logout() {
        return new AuthResponse("退出登录成功");
    }
}
