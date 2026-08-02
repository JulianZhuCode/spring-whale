package io.github.springwhale.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhaleDatabaseConfiguration {
    static {
        log.info("SpringWhaleDatabaseConfiguration loaded");
    }
}