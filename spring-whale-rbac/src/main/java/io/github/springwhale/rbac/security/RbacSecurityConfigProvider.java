package io.github.springwhale.rbac.security;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RBAC 模块默认的 Security 配置提供者
 */
@Component
@RequiredArgsConstructor
public class RbacSecurityConfigProvider implements SecurityConfigProvider {

    @Override
    public List<String> getPermitAllUrls() {
        // 合并配置文件中的 URL 和默认的 URL
        return List.of(
                "/api/rbac/auth/**",      // 认证相关接口
                "/api/rbac/public/**"     // 公共接口
        );
    }

    @Override
    public int getOrder() {
        return 100; // 默认优先级
    }
}
