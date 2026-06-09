package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门创建/更新请求
 */
@Data
public class GroupRequest {
    
    private Integer parentId;
    
    @NotBlank(message = "部门编码不能为空")
    private String code;
    
    @NotBlank(message = "部门名称不能为空")
    private String name;
    
    private String description;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}
