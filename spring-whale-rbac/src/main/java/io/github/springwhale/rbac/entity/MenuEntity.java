package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
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
     * 父菜单ID
     */
    private Integer parentId;

    /**
     * 菜单编码
     */
    @Column(nullable = false, unique = true, length = 100)
    private String code;

    /**
     * 菜单名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 菜单类型：1-目录，2-菜单，3-按钮
     */
    @Column(nullable = false)
    private Integer type = 2;

    /**
     * 路由路径
     */
    @Column(length = 200)
    private String path;

    /**
     * 组件路径
     */
    @Column(length = 200)
    private String component;

    /**
     * 权限标识
     */
    @Column(length = 100)
    private String permission;

    /**
     * 图标
     */
    @Column(length = 50)
    private String icon;

    /**
     * 排序
     */
    private Integer sort = 0;

    /**
     * 是否可见：0-隐藏，1-显示
     */
    private Integer visible = 1;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;
}
