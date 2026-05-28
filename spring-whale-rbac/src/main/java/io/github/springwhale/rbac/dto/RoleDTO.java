package io.github.springwhale.rbac.dto;

import lombok.Data;

/**
 * 角色数据传输对象
 */
@Data
public class RoleDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
}
