package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Menu entity
 */
@Entity
@Table(name = "rbac_menu", indexes = {
        @Index(name = "idx_menu_parent_id", columnList = "parent_id"),
        @Index(name = "idx_menu_code", columnList = "code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuEntity extends BaseEntity {

    /**
     * Parent menu ID
     */
    private Integer parentId;

    /**
     * Menu code
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * Menu name
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Menu type: 1=directory, 2=menu, 3=button
     */
    @Column(nullable = false)
    private Integer type = 2;

    /**
     * Route path
     */
    @Column(length = 200)
    private String path;

    /**
     * Component path
     */
    @Column(length = 200)
    private String component;

    /**
     * Permission identifier
     */
    @Column(length = 100)
    private String permission;

    /**
     * Icon
     */
    @Column(length = 50)
    private String icon;

    /**
     * Sort order
     */
    private Integer sort = 0;

    /**
     * Visibility: 0=hidden, 1=visible
     */
    private Integer visible = 1;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;
}
