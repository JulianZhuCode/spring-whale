package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Department create/update request
 */
@Data
public class GroupRequest {
    
    private Integer parentId;
    
    @NotBlank(message = "Department code must not be empty")
    private String code;
    
    @NotBlank(message = "Department name must not be empty")
    private String name;
    
    private String description;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
