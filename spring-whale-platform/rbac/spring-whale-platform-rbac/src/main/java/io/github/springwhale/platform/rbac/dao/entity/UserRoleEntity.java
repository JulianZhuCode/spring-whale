package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.SimpleBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User-role association entity
 */
@Entity
@Table(name = "rbac_user_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleEntity extends SimpleBaseEntity {

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer roleId;
}