package io.github.springwhale.rbac.dto.vo;

import lombok.Data;

/**
 * 角色菜单关联视图对象
 */
@Data
public class RoleMenuVO {
    private Integer id;
    private Integer roleId;
    private Integer menuId;
}
