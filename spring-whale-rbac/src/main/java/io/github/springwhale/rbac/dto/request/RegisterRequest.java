package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * User registration request
 */
@Data
public class RegisterRequest {

    /**
     * Username
     */
    @NotBlank(message = "Username must not be empty")
    private String username;

    /**
     * Password
     */
    @NotBlank(message = "Password must not be empty")
    private String password;

    /**
     * Real name
     */
    private String realName;

    /**
     * Email
     */
    private String email;

    /**
     * Phone
     */
    private String phone;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Department ID
     */
    private Integer groupId;
}
