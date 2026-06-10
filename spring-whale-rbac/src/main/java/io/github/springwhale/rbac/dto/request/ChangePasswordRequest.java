package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Change password request
 */
@Data
public class ChangePasswordRequest {
    
    /**
     * Old password
     */
    @NotBlank(message = "Old password must not be empty")
    private String oldPassword;
    
    /**
     * New password
     */
    @NotBlank(message = "New password must not be empty")
    private String newPassword;
}
