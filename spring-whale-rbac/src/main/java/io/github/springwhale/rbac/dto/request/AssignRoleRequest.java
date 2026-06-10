package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Assign role request
 */
@Data
public class AssignRoleRequest {
    
    /**
     * User ID
     */
    @NotNull(message = "User ID must not be empty")
    private Integer userId;
    
    /**
     * Role ID
     */
    @NotNull(message = "Role ID must not be empty")
    private Integer roleId;
}
