package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移除菜单请求
 */
@Data
public class RemoveMenuRequest {
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;
    
    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    private Integer menuId;
}
