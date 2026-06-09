package io.github.springwhale.rbac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhaleRBACConfiguration {
    static {
        log.info("SpringWhaleRBACConfiguration loaded");
    }
}
