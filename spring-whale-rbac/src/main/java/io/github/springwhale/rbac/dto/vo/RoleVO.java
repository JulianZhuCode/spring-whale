package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * Role view object
 */
@Data
public class RoleVO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
    private Integer groupId;
    private String groupName;
}
