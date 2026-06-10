package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Username must not be empty")
    private String username;
    
    @NotBlank(message = "Password must not be empty")
    private String password;
}
