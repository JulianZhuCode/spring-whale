package io.github.springwhale.rbac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan
@Slf4j
public class SpringWhaleRBACConfiguration {
    static {
        log.info("SpringWhaleRBACConfiguration loaded");
    }
}
