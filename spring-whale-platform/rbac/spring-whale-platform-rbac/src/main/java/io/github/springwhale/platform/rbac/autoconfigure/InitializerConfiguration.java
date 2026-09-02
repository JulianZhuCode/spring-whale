package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.platform.rbac.config.AdminInitializer;
import io.github.springwhale.platform.rbac.repository.GroupRepository;
import io.github.springwhale.platform.rbac.repository.MenuRepository;
import io.github.springwhale.platform.rbac.repository.RoleRepository;
import io.github.springwhale.platform.rbac.repository.UserRepository;
import io.github.springwhale.platform.rbac.repository.UserRoleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitializerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AdminInitializer adminInitializer(UserRepository userRepository,
                                             RoleRepository roleRepository,
                                             UserRoleRepository userRoleRepository,
                                             MenuRepository menuRepository,
                                             GroupRepository groupRepository,
                                             PasswordEncoder passwordEncoder) {
        return new AdminInitializer(userRepository, roleRepository, userRoleRepository,
                menuRepository, groupRepository, passwordEncoder);
    }
}