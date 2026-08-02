package io.github.springwhale.platform.rbac.dto.vo;

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
