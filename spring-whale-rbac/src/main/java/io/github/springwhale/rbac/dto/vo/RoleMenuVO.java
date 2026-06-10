package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * Role-menu association view object
 */
@Data
public class RoleMenuVO {
    private Integer id;
    private Integer roleId;
    private Integer menuId;
}
