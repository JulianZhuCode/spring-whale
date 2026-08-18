package io.github.springwhale.framework.event.kafka;

import lombok.Data;

/**
 * Kafka-specific configuration properties for the spring-whale-event framework.
 * <p>All properties are prefixed with {@code spring.whale.event.kafka}.</p>
 */
@Data
public class KafkaEventProperties {

    public static final int DEFAULT_CONCURRENCY = 1;
    public static final String DEFAULT_AUTO_OFFSET_RESET = "latest";
    public static final int DEFAULT_FAILED_CONCURRENCY = 1;
    public static final String DEFAULT_FAILED_GROUP_ID = "spring-whale-event-server";

    private int concurrency = DEFAULT_CONCURRENCY;

    private String autoOffsetReset = DEFAULT_AUTO_OFFSET_RESET;

    private int failedConcurrency = DEFAULT_FAILED_CONCURRENCY;

    private String failedGroupId = DEFAULT_FAILED_GROUP_ID;

}