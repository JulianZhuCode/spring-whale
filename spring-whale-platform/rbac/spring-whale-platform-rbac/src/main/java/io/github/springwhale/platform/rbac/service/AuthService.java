package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dto.request.LoginRequest;
import io.github.springwhale.platform.rbac.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


/**
 * Authentication service
 */
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

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
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), null);

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
}