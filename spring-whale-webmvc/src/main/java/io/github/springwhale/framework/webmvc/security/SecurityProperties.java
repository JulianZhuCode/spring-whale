package io.github.springwhale.framework.webmvc.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Security configuration properties
 */
@Data
@ConfigurationProperties(prefix = "spring.whale.web-mvc.security")
public class SecurityProperties {

    private List<String> permitAllUrls = new ArrayList<>();

    private boolean csrfEnabled = false;

    private String tokenPrefix = "Bearer ";

    private String tokenHeader = "Authorization";

    private String tokenCookieName = "sw_token";

    private String jwtSecret;

    private long jwtExpiration = 86400000;

}