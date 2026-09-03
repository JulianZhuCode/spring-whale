package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.SimpleBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role-menu association entity
 */
@Entity
@Table(name = "rbac_role_menu", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "menu_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenuEntity extends SimpleBaseEntity {

    @Column(nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private Long menuId;
}