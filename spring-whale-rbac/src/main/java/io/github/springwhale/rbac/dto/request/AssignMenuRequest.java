package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Assign menu request
 */
@Data
public class AssignMenuRequest {
    
    /**
     * Role ID
     */
    @NotNull(message = "Role ID must not be empty")
    private Integer roleId;
    
    /**
     * Menu ID
     */
    @NotNull(message = "Menu ID must not be empty")
    private Integer menuId;
}
