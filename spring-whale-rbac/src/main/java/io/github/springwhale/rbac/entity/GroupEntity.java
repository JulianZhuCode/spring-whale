package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Group (department) entity
 */
@Entity
@Table(name = "rbac_group", indexes = {
        @Index(name = "idx_group_parent_id", columnList = "parent_id"),
        @Index(name = "idx_group_code", columnList = "code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupEntity extends BaseEntity {

    /**
     * Parent department ID
     */
    private Integer parentId;

    /**
     * Department code
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Department name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Department description
     */
    @Column(length = 500)
    private String description;

    /**
     * Leader
     */
    @Column(length = 50)
    private String leader;

    /**
     * Contact phone
     */
    @Column(length = 20)
    private String phone;

    /**
     * Email
     */
    @Column(length = 100)
    private String email;

    /**
     * Sort order
     */
    private Integer sort = 0;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;
}
