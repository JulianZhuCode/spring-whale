package io.github.springwhale.rbac.service;

import io.github.springwhale.rbac.dto.LoginRequest;
import io.github.springwhale.rbac.dto.LoginResponse;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.repository.UserRepository;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. 查询用户信息
            UserEntity user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("用户不存在"));

            // 3. 生成 JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // 4. 构建响应
            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .expiresIn(86400000L) // 24小时
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    public UserEntity register(UserEntity user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("User {} changed password", userId);
    }
}
