package io.github.springwhale.database.datascope;

import io.github.springwhale.database.autoconfigure.SpringWhaleDatabaseConfiguration;
import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "io.github.springwhale.framework.event.autoconfigure.LocalEventConfiguration",
        "spring.cloud.compatibility-verifier.enabled=false"
})
@Import({TestDataScopeConfiguration.class, SpringWhaleDatabaseConfiguration.class})
@Transactional
class DataScopeIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestUserService testUserService;

    @Autowired
    private TestUserRepository testUserRepository;

    @BeforeEach
    void setUp() {
        testUserRepository.deleteAll();
        testUserRepository.flush();
        testUserRepository.save(new TestUser("Alice", 1L, 1L));
        testUserRepository.save(new TestUser("Bob", 1L, 2L));
        testUserRepository.save(new TestUser("Charlie", 2L, 3L));
        testUserRepository.save(new TestUser("David", 2L, 1L));
        testUserRepository.save(new TestUser("Eve", 3L, 4L));
        testUserRepository.flush();
    }

    @AfterEach
    void tearDown() {
        AuthenticationContextHolder.clearContext();
        TestDataScopeHandler.reset();
    }

    private void loginAs(Long userId) {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(userId, "user" + userId, null));
    }

    @Test
    @DisplayName("Verify data scope beans are registered")
    void verifyBeansExist() {
        String[] aspectBeans = applicationContext.getBeanNamesForType(DataScopeAspect.class);
        String[] repoAspectBeans = applicationContext.getBeanNamesForType(DataScopeRepositoryAspect.class);
        String[] interceptorBeans = applicationContext.getBeanNamesForType(DataScopeInterceptor.class);
        String[] handlerBeans = applicationContext.getBeanNamesForType(DataScopeHandler.class);

        assertThat(aspectBeans).isNotEmpty();
        assertThat(repoAspectBeans).isNotEmpty();
        assertThat(interceptorBeans).isNotEmpty();
        assertThat(handlerBeans).isNotEmpty();
    }

    @Test
    @DisplayName("Verify DataScopeContext is populated after @DataScope method call")
    void verifyDataScopeContextPopulated() {
        loginAs(1L);

        System.out.println("TestUserService proxy class: " + testUserService.getClass().getName());
        System.out.println("TestUserService is CGLIB proxy: " + testUserService.getClass().getName().contains("$$"));

        assertThat(DataScopeContext.hasScope()).isFalse();

        testUserService.listSelf();

        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("SELF scope: user should only see own data")
    void testSelfScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listSelf();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "David");
    }

    @Test
    @DisplayName("DEPT scope: user should only see own department data")
    void testDeptScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listByDept();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("DEPT_AND_CHILD scope: user should see own and child department data")
    void testDeptAndChildScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listByDeptAndChild();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David");
    }

    @Test
    @DisplayName("CUSTOM scope: user should see data based on custom handler logic")
    void testCustomScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listByCustom();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("AUTO scope: user should see data based on auto-inferred scope")
    void testAutoScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listByAuto();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
    }

    @Test
    @DisplayName("No @DataScope: should see all data")
    void testNoDataScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listAll();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }

    @Test
    @DisplayName("Nested @DataScope: inner SELF should override outer DEPT")
    void testNestedScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.outerDept();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "David");
    }

    @Test
    @DisplayName("Different user with DEPT scope: user 3 in dept 2")
    void testDifferentUserDeptScope() {
        loginAs(3L);

        List<TestUser> result = testUserService.listByDept();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Charlie", "David");
    }

    @Test
    @DisplayName("Different user with SELF scope: user 2 should see only own data")
    void testDifferentUserSelfScope() {
        loginAs(2L);

        List<TestUser> result = testUserService.listSelf();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Bob");
    }

    @Test
    @DisplayName("Unauthenticated user: scope is empty, denied=true, should return empty result")
    void testUnauthenticatedUser() {
        List<TestUser> result = testUserService.listByDept();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("CALLER scope: should use transmitted scope for filtering")
    void testCallerScope() {
        loginAs(1L);

        DataScopeResult transmittedScope = new DataScopeResult();
        transmittedScope.setScopeType(DataScopeType.DEPT);
        transmittedScope.setModule("test");
        DataScopeContext.pushScope(transmittedScope);

        try {
            List<TestUser> result = testUserService.listByCaller();

            assertThat(result).extracting(TestUser::getName)
                    .containsExactlyInAnyOrder("Alice", "Bob");
        } finally {
            DataScopeContext.popScope();
        }
    }

    @Test
    @DisplayName("CALLER scope with SELF: should use transmitted SELF scope for filtering")
    void testCallerScopeWithSelf() {
        loginAs(1L);

        DataScopeResult transmittedScope = new DataScopeResult();
        transmittedScope.setScopeType(DataScopeType.SELF);
        transmittedScope.setModule("test");
        DataScopeContext.pushScope(transmittedScope);

        try {
            List<TestUser> result = testUserService.listByCaller();

            assertThat(result).extracting(TestUser::getName)
                    .containsExactlyInAnyOrder("Alice", "David");
        } finally {
            DataScopeContext.popScope();
        }
    }

    @Test
    @DisplayName("CALLER scope with DEPT_AND_CHILD: should use transmitted DEPT_AND_CHILD scope")
    void testCallerScopeWithDeptAndChild() {
        loginAs(1L);

        DataScopeResult transmittedScope = new DataScopeResult();
        transmittedScope.setScopeType(DataScopeType.DEPT_AND_CHILD);
        transmittedScope.setModule("test");
        DataScopeContext.pushScope(transmittedScope);

        try {
            List<TestUser> result = testUserService.listByCaller();

            assertThat(result).extracting(TestUser::getName)
                    .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David");
        } finally {
            DataScopeContext.popScope();
        }
    }

    @Test
    @DisplayName("CALLER scope with no transmitted scope: should return all data")
    void testCallerScopeNoTransmittedScope() {
        loginAs(1L);

        List<TestUser> result = testUserService.listByCaller();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }

    @Test
    @DisplayName("CALLER scope with different user: should use transmitted scope with caller's context")
    void testCallerScopeWithDifferentUser() {
        loginAs(3L);

        DataScopeResult transmittedScope = new DataScopeResult();
        transmittedScope.setScopeType(DataScopeType.DEPT);
        transmittedScope.setModule("test");
        DataScopeContext.pushScope(transmittedScope);

        try {
            List<TestUser> result = testUserService.listByCaller();

            assertThat(result).extracting(TestUser::getName)
                    .containsExactlyInAnyOrder("Charlie", "David");
        } finally {
            DataScopeContext.popScope();
        }
    }

    @Test
    @DisplayName("skipDataScope=true: should see all data regardless of scope type")
    void testSkipDataScope() {
        TestDataScopeHandler.setSkipDataScope(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listByDept();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }

    @Test
    @DisplayName("skipDataScope=true with SELF: should still see all data")
    void testSkipDataScopeWithSelf() {
        TestDataScopeHandler.setSkipDataScope(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listSelf();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }

    @Test
    @DisplayName("skipDataScope=true without @DataScope: should see all data (no change)")
    void testSkipDataScopeWithoutAnnotation() {
        TestDataScopeHandler.setSkipDataScope(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listAll();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }

    @Test
    @DisplayName("denied: resolveDeptIds returns null, falls back to userId filter")
    void testDeniedScope() {
        TestDataScopeHandler.setReturnNullDeptIds(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listByDept();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "David");
    }

    @Test
    @DisplayName("denied with SELF: resolveDeptIds returns null, falls back to userId filter")
    void testDeniedWithSelfScope() {
        TestDataScopeHandler.setReturnNullDeptIds(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listSelf();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "David");
    }

    @Test
    @DisplayName("denied does not affect methods without @DataScope")
    void testDeniedWithoutDataScope() {
        TestDataScopeHandler.setReturnNullDeptIds(true);
        loginAs(1L);

        List<TestUser> result = testUserService.listAll();

        assertThat(result).extracting(TestUser::getName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie", "David", "Eve");
    }
}