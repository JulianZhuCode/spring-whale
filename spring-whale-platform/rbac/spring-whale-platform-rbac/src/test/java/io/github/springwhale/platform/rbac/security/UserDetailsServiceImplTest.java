package io.github.springwhale.platform.rbac.security;

import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.MenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl 单元测试")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMenuRepository roleMenuRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1);
        user.setUsername("admin");
        user.setPassword("encoded_password");
        user.setStatus(1);
    }

    @Test
    @DisplayName("正常加载用户 - 含角色和权限")
    void loadUserByUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        RoleEntity role = new RoleEntity();
        role.setId(1);
        role.setCode("ADMIN");
        role.setStatus(1);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(1);
        userRole.setRoleId(1);

        MenuEntity menu = new MenuEntity();
        menu.setId(1);
        menu.setPermission("rbac:user");
        menu.setStatus(1);

        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setRoleId(1);
        roleMenu.setMenuId(1);

        when(userRoleRepository.findByUserId(1)).thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(1))).thenReturn(List.of(role));
        when(roleMenuRepository.findByRoleIdIn(List.of(1))).thenReturn(List.of(roleMenu));
        when(menuRepository.findAllById(List.of(1))).thenReturn(List.of(menu));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("encoded_password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RbacConstants.ROLE_PREFIX + "ADMIN")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("rbac:user")));
    }

    @Test
    @DisplayName("用户不存在抛出异常")
    void loadUserByUsernameNotFound() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nobody"));
    }

    @Test
    @DisplayName("用户被禁用抛出异常")
    void loadUserByUsernameDisabled() {
        user.setStatus(0);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("admin"));
    }

    @Test
    @DisplayName("超级管理员获取通配符权限")
    void loadSuperAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        RoleEntity superAdminRole = new RoleEntity();
        superAdminRole.setId(1);
        superAdminRole.setCode(RbacConstants.SUPER_ADMIN_ROLE_CODE);
        superAdminRole.setStatus(1);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(1);
        userRole.setRoleId(1);

        when(userRoleRepository.findByUserId(1)).thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(1))).thenReturn(List.of(superAdminRole));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RbacConstants.AUTHORITY_SUPER_ADMIN)));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RbacConstants.ROLE_PREFIX + RbacConstants.SUPER_ADMIN_ROLE_CODE)));
    }

    @Test
    @DisplayName("无角色用户返回空权限")
    void loadUserWithoutRoles() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRoleRepository.findByUserId(1)).thenReturn(List.of());

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("角色被禁用时不分配权限")
    void loadUserWithDisabledRole() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        RoleEntity disabledRole = new RoleEntity();
        disabledRole.setId(1);
        disabledRole.setCode("ADMIN");
        disabledRole.setStatus(0);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(1);
        userRole.setRoleId(1);

        when(userRoleRepository.findByUserId(1)).thenReturn(List.of(userRole));
        when(roleRepository.findAllById(List.of(1))).thenReturn(List.of(disabledRole));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertTrue(result.getAuthorities().isEmpty());
    }
}