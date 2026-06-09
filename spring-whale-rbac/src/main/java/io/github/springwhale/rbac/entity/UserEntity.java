package io.github.springwhale.rbac.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
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
     * 用户名
     */
    @Column(nullable = false, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false, length = 200)
    private String password;

    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 所属部门ID
     */
    private Integer groupId;
}
