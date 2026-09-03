package io.github.springwhale.platform.rbac.security;

import io.github.springwhale.database.datascope.DataScopeCacheKey;
import io.github.springwhale.database.datascope.DataScopeProperties;
import io.github.springwhale.database.datascope.DataScopeType;
import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.repository.GroupRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleScopeViewRepository;
import io.github.springwhale.platform.rbac.dao.view.UserRoleScopeView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RBACDataScopeHandler 单元测试")
class RBACDataScopeHandlerTest {

    @Mock
    private WhaleCacheManager cacheManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleScopeViewRepository userRoleScopeViewRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DataScopeProperties properties;

    @Mock
    private WhaleCache cache;

    private RBACDataScopeHandler handler;

    @BeforeEach
    void setUp() {
        DataScopeProperties.Cache cacheProps = new DataScopeProperties.Cache();
        cacheProps.setSkipTtl(Duration.ofMinutes(5));
        cacheProps.setDeptTtl(Duration.ofMinutes(2));
        when(properties.getCache()).thenReturn(cacheProps);
        when(cacheManager.getCache("dataScope")).thenReturn(cache);

        handler = new RBACDataScopeHandler(cacheManager, userRepository,
                userRoleScopeViewRepository, roleMenuRepository, groupRepository, properties);
    }

    @Test
    @DisplayName("skipTenantScope 默认返回true")
    void skipTenantScope() {
        assertTrue(handler.skipTenantScope());
    }

    @Test
    @DisplayName("skipDataScope - 超级管理员应跳过")
    void skipDataScopeSuperAdmin() {
        UserRoleScopeView view = new UserRoleScopeView();
        view.setRoleCode(RbacConstants.SUPER_ADMIN_ROLE_CODE);

        when(cache.get(eq(DataScopeCacheKey.skipDataScope(1)), eq(Boolean.class), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());
        when(userRoleScopeViewRepository.findByUserId(1)).thenReturn(List.of(view));

        boolean result = handler.skipDataScope(1);
        assertTrue(result);
    }

    @Test
    @DisplayName("skipDataScope - 普通用户不跳过")
    void skipDataScopeNormalUser() {
        when(cache.get(eq(DataScopeCacheKey.skipDataScope(1)), eq(Boolean.class), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());
        when(userRoleScopeViewRepository.findByUserId(1)).thenReturn(List.of());

        boolean result = handler.skipDataScope(1);
        assertFalse(result);
    }

    @Test
    @DisplayName("skipDataScope - userId为null返回false")
    void skipDataScopeNullUserId() {
        boolean result = handler.skipDataScope(null);
        assertFalse(result);
    }

    @Test
    @DisplayName("resolveDeptIds - DEPT类型返回用户部门ID")
    void resolveDeptIdsDept() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setGroupId(10);

        when(cache.getList(eq(DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, null)),
                any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        List<Object> result = handler.resolveDeptIds(1, DataScopeType.DEPT, null);
        assertEquals(List.of(10), result);
    }

    @Test
    @DisplayName("resolveDeptIds - DEPT_AND_CHILD类型返回用户部门及子部门")
    void resolveDeptIdsDeptAndChild() {
        UserEntity user = new UserEntity();
        user.setId(1);
        user.setGroupId(10);

        GroupEntity child = new GroupEntity();
        child.setId(11);
        child.setParentId(10);

        when(cache.getList(eq(DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT_AND_CHILD, null)),
                any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        GroupEntity parent = new GroupEntity();
        parent.setId(10);
        parent.setPath("/");
        when(groupRepository.findById(10)).thenReturn(Optional.of(parent));
        when(groupRepository.findByPathStartingWith(eq("/10/"))).thenReturn(List.of(child));

        List<Object> result = handler.resolveDeptIds(1, DataScopeType.DEPT_AND_CHILD, null);
        assertEquals(2, result.size());
        assertTrue(result.contains(10));
        assertTrue(result.contains(11));
    }

    @Test
    @DisplayName("resolveDeptIds - userId为null返回空列表")
    void resolveDeptIdsNullUserId() {
        List<Object> result = handler.resolveDeptIds(null, DataScopeType.DEPT, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolveDeptIds - 用户不存在返回空列表")
    void resolveDeptIdsUserNotFound() {
        when(cache.getList(eq(DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, null)),
                any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        List<Object> result = handler.resolveDeptIds(1, DataScopeType.DEPT, null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolveDeptIds - SELF/CALLER类型返回空列表")
    void resolveDeptIdsSelf() {
        when(cache.getList(eq(DataScopeCacheKey.resolveDeptIds(1, DataScopeType.SELF, null)),
                any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(1)).get());

        List<Object> result = handler.resolveDeptIds(1, DataScopeType.SELF, null);
        assertTrue(result.isEmpty());
    }
}