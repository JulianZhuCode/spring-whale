package io.github.springwhale.rbac.security;

import io.github.springwhale.rbac.entity.*;
import io.github.springwhale.rbac.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详情服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final MenuRepository menuRepository;

    @Override
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.debug("Loading user details from database for: {}", username);

        // 1. 从数据库查询用户
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 2. 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用: " + username);
        }

        // 3. 从数据库查询用户的角色和权限
        List<GrantedAuthority> authorities = getUserAuthorities(user.getId());

        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * 获取用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    private List<GrantedAuthority> getUserAuthorities(Integer userId) {
        // 1. 查询用户的所有角色关联
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 2. 获取所有角色ID
        List<Integer> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());

        // 3. 查询所有角色信息
        List<RoleEntity> roles = roleRepository.findAllById(roleIds);

        // 4. 构建角色权限（ROLE_前缀）
        Set<GrantedAuthority> authorities = roles.stream()
                .filter(role -> role.getStatus() == 1) // 只包含启用的角色
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toSet());

        // 5. 查询这些角色关联的所有菜单
        List<RoleMenuEntity> roleMenus = roleIds.stream()
                .flatMap(roleId -> roleMenuRepository.findByRoleId(roleId).stream())
                .toList();

        if (!roleMenus.isEmpty()) {
            // 6. 获取所有菜单ID
            List<Integer> menuIds = roleMenus.stream()
                    .map(RoleMenuEntity::getMenuId)
                    .distinct()
                    .collect(Collectors.toList());

            // 7. 查询所有菜单信息
            List<MenuEntity> menus = menuRepository.findAllById(menuIds);

            // 8. 添加菜单权限标识
            menus.stream()
                    .filter(menu -> menu.getStatus() == 1 && menu.getPermission() != null && !menu.getPermission().isEmpty()) // 只包含启用的且有权限标识的菜单
                    .forEach(menu -> authorities.add(new SimpleGrantedAuthority(menu.getPermission())));
        }

        return new ArrayList<>(authorities);
    }

    /**
     * 清除指定用户的缓存
     * 在用户信息、角色或权限变更时调用
     *
     * @param username 用户名
     */
    @CacheEvict(value = "userDetails", key = "#username")
    public void evictUserCache(String username) {
        log.info("User cache evicted for: {}", username);
    }

    /**
     * 清除所有用户缓存
     * 慎用！仅在必要时调用
     */
    @CacheEvict(value = "userDetails", allEntries = true)
    public void evictAllUserCache() {
        log.warn("All user cache cleared");
    }
}
