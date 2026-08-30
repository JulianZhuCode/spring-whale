package io.github.springwhale.database.datascope;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestUserNestedService {

    private final TestUserRepository repository;

    @DataScope(scopeType = DataScopeType.SELF, module = "test")
    public List<TestUser> innerSelf() {
        return repository.findAll();
    }
}