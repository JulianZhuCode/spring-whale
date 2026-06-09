package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * 角色视图对象
 */
@Data
public class RoleVO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
}
