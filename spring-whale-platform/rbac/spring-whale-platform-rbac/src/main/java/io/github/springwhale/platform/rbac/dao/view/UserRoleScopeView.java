package io.github.springwhale.platform.rbac.dao.view;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * Read-only view: user → role → data scope → custom departments.
 * <p>
 * One row per (user_role, dept) combination. Roles without custom depts
 * still produce one row with deptGroupId = 0.
 */
@Entity
@Immutable
@Table(name = "rbac_user_role_scope_view")
@IdClass(UserRoleScopeView.UserRoleScopeViewId.class)
@Data
@EqualsAndHashCode(of = {"userRoleId", "deptGroupId"})
public class UserRoleScopeView {

    @Id
    @Column(name = "user_role_id")
    private Long userRoleId;

    @Id
    @Column(name = "dept_group_id")
    private Long deptGroupId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "data_scope")
    private String dataScope;

    @Column(name = "role_code")
    private String roleCode;

    @Data
    public static class UserRoleScopeViewId implements Serializable {
        private Long userRoleId;
        private Long deptGroupId;
    }
}