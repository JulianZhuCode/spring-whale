package io.github.springwhale.framework.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Core auto-configuration for Spring Whale Framework.
 * <p>
 * Scans all framework modules for beans while excluding classes
 * under {@code autoconfigure} sub-packages. Those are loaded
 * separately via {@code AutoConfiguration.imports} which enables
 * proper auto-configuration ordering (e.g. {@code @AutoConfigureBefore}).
 * </p>
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.springwhale",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "io\\.github\\.springwhale\\..*\\.autoconfigure\\..*"
        ))
@Slf4j
public class SpringWhaleCoreConfiguration {
    static {
        log.info("SpringWhaleCoreConfiguration loaded");
    }
}
