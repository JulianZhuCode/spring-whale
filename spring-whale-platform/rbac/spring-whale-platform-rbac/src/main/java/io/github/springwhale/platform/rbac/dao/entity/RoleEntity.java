package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.database.datascope.annotation.DeptIdField;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role entity
 */
@Entity
@Table(name = "rbac_role")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends BaseEntity {

    /**
     * Role code, optional — used for role-based access control (ROLE_ prefix).
     * When empty, the role only grants menu-level permissions.
     */
    @Column(unique = true)
    private String code;

    /**
     * Role name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Role description
     */
    private String description;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * Sort order
     */
    private Integer sort = 0;

    /**
     * Group (department) ID
     */
    @DeptIdField
    private Integer groupId;

    @Enumerated(EnumType.STRING)
    private DataScopeType dataScope;
}