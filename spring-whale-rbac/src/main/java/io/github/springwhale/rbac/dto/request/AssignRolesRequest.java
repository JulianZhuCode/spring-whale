package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量分配角色请求
 */
@Data
public class AssignRolesRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
    
    /**
     * 角色ID列表
     */
    @NotEmpty(message = "角色ID列表不能为空")
    private List<Integer> roleIds;
}
