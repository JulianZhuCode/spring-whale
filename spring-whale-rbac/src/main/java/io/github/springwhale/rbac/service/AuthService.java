package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.ChangePasswordRequest;
import io.github.springwhale.rbac.dto.request.LoginRequest;
import io.github.springwhale.rbac.dto.request.RegisterRequest;
import io.github.springwhale.rbac.dto.response.LoginResponse;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.mapper.UserMapper;
import io.github.springwhale.rbac.repository.UserRepository;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final UserMapper userMapper;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. 验证用户名和密码（如果失败会抛出 BadCredentialsException）
            authenticationManager.authenticate(
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
    public UserVO register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw BusinessException.create("USER_ALREADY_EXISTS", "用户名已存在");
        }

        // 转换为 Entity
        UserEntity user = userMapper.toEntity(new UserVO());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setGroupId(request.getGroupId());
        
        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        UserEntity savedUser = userRepository.save(user);
        
        // 转换为 VO
        return userMapper.toVO(savedUser);
    }

    /**
     * 修改密码
     */
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.create("OLD_PASSWORD_INCORRECT", "旧密码错误");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("User {} changed password", userId);
    }
}
