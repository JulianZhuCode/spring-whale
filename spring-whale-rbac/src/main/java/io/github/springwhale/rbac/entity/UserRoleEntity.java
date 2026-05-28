package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.SimpleBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联实体
 */
@Entity
@Table(name = "rbac_user_role", schema = "rbac", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleEntity extends SimpleBaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Integer roleId;
}
