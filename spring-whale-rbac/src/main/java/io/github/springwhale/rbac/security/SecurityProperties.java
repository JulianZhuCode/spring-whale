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
@ConfigurationProperties(prefix = "spring.whale.rbac.security")
public class SecurityProperties {

    private List<String> permitAllUrls = new ArrayList<>();

    private List<String> allowedOriginPatterns = new ArrayList<>();

    private boolean csrfEnabled = false;

    private boolean corsEnabled = true;

    private String tokenPrefix = "Bearer ";

    private String tokenHeader = "Authorization";
}
