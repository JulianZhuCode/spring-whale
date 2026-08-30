package io.github.springwhale.database.datascope;

import lombok.RequiredArgsConstructor;
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

    public TestUser save(TestUser user) {
        return repository.save(user);
    }
}