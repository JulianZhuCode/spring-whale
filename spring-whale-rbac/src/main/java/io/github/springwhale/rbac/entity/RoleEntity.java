package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role entity
 */
@Entity
@Table(name = "rbac_role", indexes = {
        @Index(name = "idx_role_code", columnList = "code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends BaseEntity {

    /**
     * Role code
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Role name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Role description
     */
    @Column(length = 500)
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
}
