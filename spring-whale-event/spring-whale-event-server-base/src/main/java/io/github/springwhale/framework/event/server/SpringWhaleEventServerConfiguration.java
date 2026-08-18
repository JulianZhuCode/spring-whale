package io.github.springwhale.framework.event.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhaleEventServerConfiguration {
    static {
        log.debug("SpringWhaleEventServerConfiguration loaded");
    }
}