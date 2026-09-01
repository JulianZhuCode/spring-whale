package io.github.springwhale.framework.core.autoconfigure;

import io.github.springwhale.framework.core.json.*;
import io.github.springwhale.framework.core.json.serializer.I18nSerializer;
import io.github.springwhale.framework.core.utils.EdgeTtsUtil;
import io.github.springwhale.framework.core.utils.SpringContextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Core auto-configuration for Spring Whale Framework.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning, ensuring deterministic
 * registration order and clear dependency tracking.
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SpringWhaleJsonProperties.class)
public class SpringWhaleCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtils springContextUtils() {
        return new SpringContextUtils();
    }

    @Bean
    @ConditionalOnMissingBean
    public EdgeTtsUtil edgeTtsUtil(
            @Value("${edge-tts.command:edge-tts}") String command,
            @Value("${edge-tts.timeout-seconds:30}") int timeoutSeconds,
            @Value("${edge-tts.concurrency:0}") int concurrency) {
        return new EdgeTtsUtil(command, timeoutSeconds, concurrency);
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nSerializer i18nSerializer() {
        return new I18nSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public BigDecimalJacksonComponent bigDecimalJacksonComponent() {
        return new BigDecimalJacksonComponent();
    }

    @Bean
    @ConditionalOnMissingBean
    public NumberJacksonComponent numberJacksonComponent() {
        return new NumberJacksonComponent();
    }

    @Bean
    @ConditionalOnMissingBean
    public DateTimeJacksonComponent dateTimeJacksonComponent() {
        return new DateTimeJacksonComponent();
    }

    @Bean
    @ConditionalOnMissingBean
    public BaseEnumJacksonComponent baseEnumJacksonComponent() {
        return new BaseEnumJacksonComponent();
    }
}