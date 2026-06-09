package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * 用户角色关联视图对象
 */
@Data
public class UserRoleVO {
    private Integer id;
    private Integer userId;
    private Integer roleId;
}
