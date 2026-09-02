package io.github.springwhale.platform.rbac.dao.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "rbac_role_dept", indexes = {
        @Index(name = "idx_role_dept_role_id", columnList = "role_id"),
        @Index(name = "idx_role_dept_group_id", columnList = "group_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDeptEntity extends BaseEntity {

    private Integer roleId;

    private Integer groupId;
}