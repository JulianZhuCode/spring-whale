package io.github.springwhale.rbac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableCaching
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhaleRBACConfiguration {
    static {
        log.info("SpringWhaleRBACConfiguration loaded");
    }
}
