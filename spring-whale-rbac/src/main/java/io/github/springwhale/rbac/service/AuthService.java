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
 * Authentication service
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
     * User login
     */
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. Authenticate username and password (throws BadCredentialsException on failure)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Query user info
            UserEntity user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // 3. Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // 4. Build response
            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .expiresIn(86400000L) // 24 hours
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    /**
     * User registration
     */
    public UserVO register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw BusinessException.create("USER_ALREADY_EXISTS", "Username already exists");
        }

        // Convert to Entity
        UserEntity user = userMapper.toEntity(new UserVO());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setGroupId(request.getGroupId());
        
        // Set default status
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        UserEntity savedUser = userRepository.save(user);
        
        // Convert to VO
        return userMapper.toVO(savedUser);
    }

    /**
     * Change password
     */
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.create("OLD_PASSWORD_INCORRECT", "Old password is incorrect");
        }

        // Update new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("User {} changed password", userId);
    }
}
