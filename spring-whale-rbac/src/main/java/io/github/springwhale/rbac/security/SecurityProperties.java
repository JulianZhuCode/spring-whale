package io.github.springwhale.rbac.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Security 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "rbac.security")
public class SecurityProperties {

    /**
     * 允许匿名访问的接口路径列表
     * 支持 Ant 风格的路径匹配：
     * - /api/rbac/auth/** 匹配所有子路径
     * - /api/rbac/public/* 匹配一级子路径
     * - /api/test 精确匹配
     */
    private List<String> permitAllUrls = new ArrayList<>();

    /**
     * 是否启用 CSRF 保护（默认禁用，JWT 场景下通常不需要）
     */
    private boolean csrfEnabled = false;

    /**
     * 是否启用 CORS 跨域支持
     */
    private boolean corsEnabled = true;

    /**
     * JWT Token 前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * JWT Token Header 名称
     */
    private String tokenHeader = "Authorization";
}
