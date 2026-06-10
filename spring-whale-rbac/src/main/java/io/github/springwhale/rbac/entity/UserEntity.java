package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User entity
 */
@Entity
@Table(name = "rbac_user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {

    /**
     * Username
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * Password
     */
    @Column(nullable = false, length = 200)
    private String password;

    /**
     * Real name
     */
    @Column(length = 50)
    private String realName;

    /**
     * Email
     */
    @Column(length = 100)
    private String email;

    /**
     * Phone
     */
    @Column(length = 20)
    private String phone;

    /**
     * Avatar URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * Status: 0=disabled, 1=enabled
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * Department ID
     */
    private Integer groupId;
}
