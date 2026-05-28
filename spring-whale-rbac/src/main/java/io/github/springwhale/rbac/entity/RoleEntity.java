package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Entity
@Table(name = "rbac_role", schema = "rbac", indexes = {
        @Index(name = "idx_role_code", columnList = "code")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleEntity extends BaseEntity {

    /**
     * 角色编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 角色描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 排序
     */
    private Integer sort = 0;
}
