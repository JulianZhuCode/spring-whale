package io.github.springwhale.framework.thymeleaf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Admin console configuration properties.
 * <p>
 * Allows customizing the admin console brand name, short name,
 * and copyright text displayed in the sidebar, login page,
 * and error pages.
 * </p>
 *
 * <pre>{@code
 * spring.whale.thymeleaf.admin.brand-name=My App
 * spring.whale.thymeleaf.admin.short-name=MA
 * spring.whale.thymeleaf.admin.copyright=My Company
 * }</pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.thymeleaf.admin")
public class AdminProperties {

    /**
     * Full brand name displayed in page titles and login page.
     * Default: "Spring Whale"
     */
    private String brandName = "Spring Whale";

    /**
     * Short name displayed in the sidebar.
     * Default: "SW Admin"
     */
    private String shortName = "SW Admin";

    /**
     * Copyright text displayed in footers.
     * Default: "Spring Whale Framework"
     */
    private String copyright = "Spring Whale Framework";

    /**
     * Version string displayed in the header.
     * Default: "0.0.2"
     */
    private String version = "0.0.2";
}
