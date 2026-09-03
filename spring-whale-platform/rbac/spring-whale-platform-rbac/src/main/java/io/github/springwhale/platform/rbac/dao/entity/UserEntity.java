package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.BaseEntity;
import io.github.springwhale.database.datascope.annotation.DeptIdField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User entity
 */
@Entity
@Table(name = "rbac_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {

    /**
     * Username
     */
    @Column(nullable = false)
    private String username;

    /**
     * Password
     */
    @Column(nullable = false)
    private String password;

    /**
     * Real name
     */
    private String realName;

    /**
     * Email
     */
    private String email;

    /**
     * Phone
     */
    private String phone;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * Department ID
     */
    @DeptIdField
    private Integer groupId;
}