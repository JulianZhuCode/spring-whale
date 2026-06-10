package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Batch assign menus request
 */
@Data
public class AssignMenusRequest {
    
    /**
     * Role ID
     */
    @NotNull(message = "Role ID must not be empty")
    private Integer roleId;
    
    /**
     * Menu ID list
     */
    @NotEmpty(message = "Menu ID list must not be empty")
    private List<Integer> menuIds;
}
