package io.github.springwhale.framework.thymeleaf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the spring-whale-thymeleaf module.
 * <p>
 * Scans the framework's own packages for components such as
 * {@code AdminMenuProvider} implementations, security config providers,
 * and admin page controllers. Business modules that depend on this
 * module only need to provide their own templates and menu registrations.
 * </p>
 */
@Configuration
@ComponentScan(basePackages = "io.github.springwhale.framework.thymeleaf")
public class SpringWhaleThymeleafConfiguration {
}
