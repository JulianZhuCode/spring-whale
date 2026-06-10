package io.github.springwhale.framework.webmvc.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Security configuration properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.web-mvc.security")
public class SecurityProperties {

    private List<String> permitAllUrls = new ArrayList<>();

    private List<String> allowedOriginPatterns = new ArrayList<>();

    private boolean csrfEnabled = false;

    private boolean corsEnabled = true;

    private String tokenPrefix = "Bearer ";

    private String tokenHeader = "Authorization";

    private String jwtSecret = "SpringWhaleSecretKey2024ForJWTTokenGeneration";

    private long jwtExpiration = 86400000;
}
