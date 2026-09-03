package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "rbac_role_dept")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDeptEntity extends BaseEntity {

    @Column(nullable = false)
    private Integer roleId;

    @Column(nullable = false)
    private Integer groupId;
}