package io.github.springwhale.database.datascope;

import jakarta.persistence.*;

@Entity
@Table(name = "test_user")
public class TestUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @DeptIdField
    private Long deptId;

    @UserIdField
    private Long createBy;

    public TestUser() {
    }

    public TestUser(String name, Long deptId, Long createBy) {
        this.name = name;
        this.deptId = deptId;
        this.createBy = createBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }
}