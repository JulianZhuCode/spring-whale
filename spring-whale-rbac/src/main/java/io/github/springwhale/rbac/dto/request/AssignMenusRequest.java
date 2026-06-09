package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量分配菜单请求
 */
@Data
public class AssignMenusRequest {
    
    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;
    
    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Integer> menuIds;
}
