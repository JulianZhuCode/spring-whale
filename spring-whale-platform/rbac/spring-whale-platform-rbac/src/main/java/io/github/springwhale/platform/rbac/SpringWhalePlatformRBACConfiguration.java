package io.github.springwhale.platform.rbac;

import io.github.springwhale.database.autoconfigure.SpringWhaleDatabaseConfiguration;
import io.github.springwhale.platform.rbac.autoconfigure.ControllerConfiguration;
import io.github.springwhale.platform.rbac.autoconfigure.SecurityConfiguration;
import io.github.springwhale.platform.rbac.autoconfigure.ServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfigureBefore(SpringWhaleDatabaseConfiguration.class)
@AutoConfiguration
@EnableCaching
@EnableJpaRepositories
@EntityScan
@Import({
        ServiceConfiguration.class,
        SecurityConfiguration.class,
        ControllerConfiguration.class
})
@Slf4j
public class SpringWhalePlatformRBACConfiguration {
}