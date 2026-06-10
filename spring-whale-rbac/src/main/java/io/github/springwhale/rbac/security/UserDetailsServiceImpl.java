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
 * User details service
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

        // 1. Query user from database
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Check user status
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }

        // 3. Query user roles and permissions from database
        List<GrantedAuthority> authorities = getUserAuthorities(user.getId());

        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * Get user authorities
     *
     * @param userId User ID
     * @return list of authorities
     */
    private List<GrantedAuthority> getUserAuthorities(Integer userId) {
        // 1. Query all role associations for the user
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 2. Get all role IDs
        List<Integer> roleIds = userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());

        // 3. Query all role information
        List<RoleEntity> roles = roleRepository.findAllById(roleIds);

        // 4. Build role authorities (ROLE_ prefix)
        Set<GrantedAuthority> authorities = roles.stream()
                .filter(role -> role.getStatus() == 1) // Only include enabled roles
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toSet());

        // 5. Query all menus associated with these roles
        List<RoleMenuEntity> roleMenus = roleIds.stream()
                .flatMap(roleId -> roleMenuRepository.findByRoleId(roleId).stream())
                .toList();

        if (!roleMenus.isEmpty()) {
            // 6. Get all menu IDs
            List<Integer> menuIds = roleMenus.stream()
                    .map(RoleMenuEntity::getMenuId)
                    .distinct()
                    .collect(Collectors.toList());

            // 7. Query all menu information
            List<MenuEntity> menus = menuRepository.findAllById(menuIds);

            // 8. Add menu permission identifiers
            menus.stream()
                    .filter(menu -> menu.getStatus() == 1 && menu.getPermission() != null && !menu.getPermission().isEmpty()) // Only include enabled menus with permission identifiers
                    .forEach(menu -> authorities.add(new SimpleGrantedAuthority(menu.getPermission())));
        }

        return new ArrayList<>(authorities);
    }

    /**
     * Evict cache for specified user
     * Called when user info, roles, or permissions change
     *
     * @param username Username
     */
    @CacheEvict(value = "userDetails", key = "#username")
    public void evictUserCache(String username) {
        log.info("User cache evicted for: {}", username);
    }

    /**
     * Evict all user cache
     * Use with caution! Only when necessary
     */
    @CacheEvict(value = "userDetails", allEntries = true)
    public void evictAllUserCache() {
        log.warn("All user cache cleared");
    }
}
