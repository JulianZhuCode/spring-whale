package io.github.springwhale.rbac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT token
     */
    private String token;

    /**
     * Token type
     */
    private String tokenType = "Bearer";

    /**
     * User ID
     */
    private Integer userId;

    /**
     * Username
     */
    private String username;

    /**
     * Real name
     */
    private String realName;

    /**
     * Expiration time (milliseconds)
     */
    private Long expiresIn;
}
