package io.github.springwhale.rbac.security;

import io.github.springwhale.rbac.entity.UserEntity;
import io.github.springwhale.rbac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户详情服务
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用: " + username);
        }

        // TODO: 从数据库查询用户的角色和权限
        // 这里暂时返回空列表，后续可以从 Role 和 Menu 表中查询
        List<GrantedAuthority> authorities = List.of();

        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
