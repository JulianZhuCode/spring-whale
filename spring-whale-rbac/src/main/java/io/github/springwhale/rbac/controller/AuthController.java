package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.LoginRequest;
import io.github.springwhale.rbac.dto.LoginResponse;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 用户注册
     * POST /api/rbac/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) {
        return ResponseEntity.ok(authService.register(user));
    }

    /**
     * 获取当前用户信息
     * GET /api/rbac/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 修改密码
     * POST /api/rbac/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> passwords,
                                                Authentication authentication) {
        // 从 SecurityContext 获取当前用户ID
        // TODO: 需要从 UserDetails 中获取 userId
        Integer userId = 1; // 临时硬编码，后续优化
        
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        
        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    /**
     * 退出登录（前端删除 Token 即可）
     * POST /api/rbac/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "退出登录成功");
        return ResponseEntity.ok(result);
    }
}
