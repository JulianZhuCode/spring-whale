package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分组（部门）实体
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
     * 父部门ID
     */
    private Integer parentId;

    /**
     * 部门编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 部门名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 部门描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 负责人
     */
    @Column(length = 50)
    private String leader;

    /**
     * 联系电话
     */
    @Column(length = 20)
    private String phone;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 排序
     */
    private Integer sort = 0;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;
}
