package io.github.springwhale.framework.event.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Micrometer-based event metrics.
 * <p>Activated when {@code MeterRegistry} (from Micrometer) is on the classpath,
 * which is the case when {@code spring-boot-starter-actuator} is included.</p>
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class MicrometerEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MicrometerEventMetricsCollector micrometerEventMetricsCollector(MeterRegistry meterRegistry) {
        return new MicrometerEventMetricsCollector(meterRegistry);
    }
}