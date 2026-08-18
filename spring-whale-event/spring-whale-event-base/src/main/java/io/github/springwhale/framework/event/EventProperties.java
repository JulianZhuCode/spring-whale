package io.github.springwhale.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the spring-whale-event framework.
 * <p>Registered as a {@code @Component} (not just {@code @ConfigurationProperties}) because
 * this module is only included when the event framework is actively used — there is no
 * scenario where the module is on the classpath but the framework is not needed.</p>
 * <p>All properties are prefixed with {@code spring.whale.event}.</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.event")
public class EventProperties {

    public static final String DEFAULT_EVENT_TOPIC = "EVENT_TOPIC";
    public static final String DEFAULT_FAILED_TOPIC = "EVENT_FAILED_TOPIC";
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_RETRY_INTERVAL = 5;
    public static final int DEFAULT_RETRY_BATCH_SIZE = 1000;
    public static final int DEFAULT_RETRY_SCHEDULE_INTERVAL = 60000;
    public static final int DEFAULT_SEND_TIMEOUT_SECONDS = 3;
    public static final int DEFAULT_RETRY_THREAD_POOL_SIZE = 4;
    public static final int DEFAULT_RETRY_CLEANUP_RETENTION_DAYS = 30;
    public static final int DEFAULT_RETRY_CLEANUP_BATCH_SIZE = 500;
    public static final int DEFAULT_RETRY_CLEANUP_SCHEDULE_INTERVAL = 86_400_000;

    private String eventTopic = DEFAULT_EVENT_TOPIC;

    private String failedTopic = DEFAULT_FAILED_TOPIC;

    private String listener = DEFAULT_EVENT_TOPIC;

    private int maxRetries = DEFAULT_MAX_RETRIES;

    private int retryInterval = DEFAULT_RETRY_INTERVAL;

    private int retryBatchSize = DEFAULT_RETRY_BATCH_SIZE;

    private int retryScheduleInterval = DEFAULT_RETRY_SCHEDULE_INTERVAL;

    private int sendTimeoutSeconds = DEFAULT_SEND_TIMEOUT_SECONDS;

    private int retryThreadPoolSize = DEFAULT_RETRY_THREAD_POOL_SIZE;

    private int retryCleanupRetentionDays = DEFAULT_RETRY_CLEANUP_RETENTION_DAYS;

    private int retryCleanupBatchSize = DEFAULT_RETRY_CLEANUP_BATCH_SIZE;

    private int retryCleanupScheduleInterval = DEFAULT_RETRY_CLEANUP_SCHEDULE_INTERVAL;

}