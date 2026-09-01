package io.github.springwhale.platform.task.ui;

import io.github.springwhale.platform.task.ui.menu.TaskMenuProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Spring Whale Platform Task UI module.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning.
 * </p>
 */
@AutoConfiguration
public class SpringWhalePlatformTaskUIConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TaskMenuProvider taskMenuProvider() {
        return new TaskMenuProvider();
    }
}