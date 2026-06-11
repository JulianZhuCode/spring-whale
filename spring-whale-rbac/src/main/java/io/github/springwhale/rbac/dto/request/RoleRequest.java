package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Role create/update request
 */
@Data
public class RoleRequest {
    
    @NotBlank(message = "Role code must not be empty")
    private String code;
    
    @NotBlank(message = "Role name must not be empty")
    private String name;
    
    private String description;
    private Integer status;
    private Integer sort;
    private Integer groupId;
}
