package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移除角色请求
 */
@Data
public class RemoveRoleRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;
}
