package io.github.springwhale.framework.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.springwhale")
@Slf4j
public class SpringWhaleCoreConfiguration {
    static {
        log.info("SpringWhaleCoreConfiguration loaded");
    }
}
