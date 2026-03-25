package io.github.springwhale.framework.core;

import io.github.springwhale.framework.core.json.SpringWhaleJsonConfig;
import io.github.springwhale.framework.core.json.SpringWhaleJacksonComponent;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        SpringWhaleJsonConfig.class,
        SpringWhaleJacksonComponent.class,
        SpringContextUtils.class
})
@Slf4j
public class SpringWhaleCoreConfiguration {
    static {
        log.info("SpringWhaleCoreConfiguration loaded");
    }
}
