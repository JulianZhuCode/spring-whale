package io.github.springwhale.database.datascope;

import io.github.springwhale.database.datascope.annotation.DataScope;
import io.github.springwhale.database.datascope.annotation.SkipSqlInspector;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestUserService {

    private final TestUserRepository repository;
    private final TestUserNestedService nestedService;

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    public List<TestUser> listSelf() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> listByDept() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.DEPT_AND_CHILD, module = "test")
    public List<TestUser> listByDeptAndChild() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.CUSTOM, module = "test")
    public List<TestUser> listByCustom() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.AUTO, module = "test")
    public List<TestUser> listByAuto() {
        return repository.findAll();
    }

    public List<TestUser> listAll() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> outerDept() {
        return nestedService.innerSelf();
    }

    @DataScope(scopeType = DataScopeType.CALLER, module = "test")
    public List<TestUser> listByCaller() {
        return repository.findAll();
    }

    public TestUser save(TestUser user) {
        return repository.save(user);
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> listByDeptDerived() {
        return repository.findByDeptId(1L);
    }

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    public List<TestUser> listSelfDerived() {
        return repository.findByDeptId(1L);
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> listByDeptQuery() {
        return repository.findByDeptIdQuery(1L);
    }

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    public List<TestUser> listSelfQuery() {
        return repository.findByCreateByQuery(1L);
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> listByDeptSpec() {
        Specification<TestUser> spec = (root, query, cb) -> cb.equal(root.get("deptId"), 1L);
        return repository.findAll(spec);
    }

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    public List<TestUser> listSelfSpec() {
        Specification<TestUser> spec = (root, query, cb) -> cb.equal(root.get("createBy"), 1L);
        return repository.findAll(spec);
    }

    public List<TestUser> listAllSpec() {
        Specification<TestUser> spec = (root, query, cb) -> cb.conjunction();
        return repository.findAll(spec);
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    @SkipSqlInspector
    public List<TestUser> listByDeptSkipSqlInspector() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    @SkipSqlInspector
    public List<TestUser> listSelfSkipSqlInspector() {
        return repository.findAll();
    }

    @SkipSqlInspector
    public List<TestUser> listAllSkipSqlInspector() {
        return repository.findAll();
    }

    @DataScope(scopeType = DataScopeType.DEPT, module = "test")
    public List<TestUser> listByDeptRepoSkip() {
        return repository.findByDeptIdOrCreateBy(1L, 1L);
    }
}