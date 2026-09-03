package io.github.springwhale.platform.rbac.dto.vo;

import io.github.springwhale.database.datascope.DataScopeType;
import lombok.Data;

/**
 * Role view object
 */
@Data
public class RoleVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Integer sort;
    private Long groupId;
    private String groupName;
    private DataScopeType dataScope;
}