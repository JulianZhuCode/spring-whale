package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.platform.rbac.dto.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dto.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.dto.mapper.RoleMapper;
import io.github.springwhale.platform.rbac.dto.mapper.UserMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserMapper userMapper() {
        return new UserMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMapper roleMapper() {
        return new RoleMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuMapper menuMapper() {
        return new MenuMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupMapper groupMapper() {
        return new GroupMapper();
    }
}