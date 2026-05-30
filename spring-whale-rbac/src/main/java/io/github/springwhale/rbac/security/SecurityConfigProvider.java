package io.github.springwhale.rbac.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/**
 * Security 配置提供者接口
 * <p>
 * 其他模块可以实现此接口来扩展安全配置
 * </p>
 *
 * @example
 * <pre>{@code
 * @Component
 * public class CustomSecurityConfigProvider implements SecurityConfigProvider {
 *     @Override
 *     public List<String> getPermitAllUrls() {
 *         return List.of("/api/custom/public/**");
 *     }
 * }
 * }</pre>
 */
public interface SecurityConfigProvider {

    /**
     * 获取允许匿名访问的 URL 列表
     *
     * @return URL 路径列表，支持 Ant 风格匹配
     */
    default List<String> getPermitAllUrls() {
        return List.of();
    }

    /**
     * 自定义 HttpSecurity 配置
     * <p>
     * 可以在这里添加更复杂的 security 配置
     * </p>
     *
     * @param http HttpSecurity 对象
     * @throws Exception 配置异常
     */
    default void configure(HttpSecurity http) throws Exception {
        // 默认不做任何配置
    }

    /**
     * 获取自定义的 RequestMatcher
     * <p>
     * 用于更复杂的路径匹配逻辑
     * </p>
     *
     * @return RequestMatcher 列表
     */
    default List<RequestMatcher> getCustomRequestMatchers() {
        return List.of();
    }

    /**
     * 提供者优先级（数值越小优先级越高）
     *
     * @return 优先级
     */
    default int getOrder() {
        return 0;
    }
}
