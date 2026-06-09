package io.github.springwhale.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单创建/更新请求
 */
@Data
public class MenuRequest {
    
    private Integer parentId;
    
    @NotBlank(message = "菜单编码不能为空")
    private String code;
    
    @NotBlank(message = "菜单名称不能为空")
    private String name;
    
    @NotNull(message = "菜单类型不能为空")
    private Integer type;
    
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer visible;
    private Integer status;
}
