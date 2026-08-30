package io.github.springwhale.framework.event.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka-specific configuration properties for the spring-whale-event framework.
 * <p>All properties are prefixed with {@code spring.whale.event.kafka}.</p>
 */
@Data
@ConfigurationProperties(prefix = "spring.whale.event.kafka")
public class KafkaEventProperties {

    public static final String DEFAULT_AUTO_OFFSET_RESET = "latest";

    private String autoOffsetReset = DEFAULT_AUTO_OFFSET_RESET;

}