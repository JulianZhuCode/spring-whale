package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.SimpleBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体
 */
@Entity
@Table(name = "rbac_role_menu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "menu_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenuEntity extends SimpleBaseEntity {

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    /**
     * 菜单ID
     */
    @Column(name = "menu_id", nullable = false)
    private Integer menuId;
}
