package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * User-role association view object
 */
@Data
public class UserRoleVO {
    private Integer id;
    private Integer userId;
    private Integer roleId;
}
