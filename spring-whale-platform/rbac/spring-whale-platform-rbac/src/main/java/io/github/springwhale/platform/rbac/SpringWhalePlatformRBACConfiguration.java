package io.github.springwhale.platform.rbac;

import io.github.springwhale.platform.rbac.autoconfigure.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableCaching
@EnableJpaRepositories
@EntityScan
@Import({
        MapperConfiguration.class,
        ServiceConfiguration.class,
        SecurityConfiguration.class,
        ControllerConfiguration.class,
        InitializerConfiguration.class
})
@Slf4j
public class SpringWhalePlatformRBACConfiguration {
}