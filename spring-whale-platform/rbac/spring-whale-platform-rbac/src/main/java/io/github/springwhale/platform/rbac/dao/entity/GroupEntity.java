package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.database.datascope.annotation.DeptIdScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Group (department) entity
 */
@Entity
@DeptIdScope
@Table(name = "rbac_group")
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupEntity extends BaseEntity {

    /**
     * Parent department ID
     */
    private Long parentId;

    /**
     * Materialized path of ancestor IDs, e.g. "/1/3/".
     * Used for efficient descendant queries: {@code WHERE path LIKE '/1/3/%'}.
     * Root nodes use "/" as path.
     */
    private String path;

    /**
     * Department code
     */
    @Column(unique = true)
    private String code;

    /**
     * Department name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Department description
     */
    private String description;

    /**
     * Leader
     */
    private String leader;

    /**
     * Contact phone
     */
    private String phone;

    /**
     * Email
     */
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