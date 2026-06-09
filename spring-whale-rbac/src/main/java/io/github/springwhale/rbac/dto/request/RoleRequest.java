package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建/更新请求
 */
@Data
public class RoleRequest {
    
    @NotBlank(message = "角色编码不能为空")
    private String code;
    
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    private String description;
    private Integer status;
    private Integer sort;
}
