package io.github.springwhale.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the spring-whale-event framework.
 * <p>All properties are prefixed with {@code spring.whale.event}.</p>
 */
@Data
@ConfigurationProperties(prefix = "spring.whale.event")
public class EventProperties {

    public static final String DEFAULT_EVENT_TOPIC = "EVENT_TOPIC";
    public static final String DEFAULT_FAILED_TOPIC = "EVENT_FAILED_TOPIC";
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_RETRY_INTERVAL_SECONDS = 5;
    public static final int DEFAULT_RETRY_BATCH_SIZE = 1000;
    public static final int DEFAULT_RETRY_SCHEDULE_INTERVAL = 60000;
    public static final int DEFAULT_SEND_TIMEOUT_SECONDS = 3;
    public static final int DEFAULT_RETRY_THREAD_POOL_SIZE = 4;
    public static final int DEFAULT_RETRY_CLEANUP_RETENTION_DAYS = 30;
    public static final int DEFAULT_RETRY_CLEANUP_BATCH_SIZE = 500;
    public static final int DEFAULT_RETRY_CLEANUP_SCHEDULE_INTERVAL = 86_400_000;
    public static final int DEFAULT_RETRY_MAX_INTERVAL = 300;
    public static final String DEFAULT_RETRY_STRATEGY = "fixed";
    public static final int DEFAULT_CONCURRENCY = 1;
    public static final int DEFAULT_FAILED_CONCURRENCY = 1;
    public static final String DEFAULT_FAILED_GROUP_ID = "spring-whale-event-recovery";

    private String eventTopic = DEFAULT_EVENT_TOPIC;

    private String failedTopic = DEFAULT_FAILED_TOPIC;

    private String consumerTopics = DEFAULT_EVENT_TOPIC;

    private int maxRetries = DEFAULT_MAX_RETRIES;

    private int retryIntervalSeconds = DEFAULT_RETRY_INTERVAL_SECONDS;

    private int retryBatchSize = DEFAULT_RETRY_BATCH_SIZE;

    private int retryScheduleInterval = DEFAULT_RETRY_SCHEDULE_INTERVAL;

    private int sendTimeoutSeconds = DEFAULT_SEND_TIMEOUT_SECONDS;

    private int retryThreadPoolSize = DEFAULT_RETRY_THREAD_POOL_SIZE;

    private int retryCleanupRetentionDays = DEFAULT_RETRY_CLEANUP_RETENTION_DAYS;

    private int retryCleanupBatchSize = DEFAULT_RETRY_CLEANUP_BATCH_SIZE;

    private int retryCleanupScheduleInterval = DEFAULT_RETRY_CLEANUP_SCHEDULE_INTERVAL;

    private String retryStrategy = DEFAULT_RETRY_STRATEGY;

    private int retryMaxInterval = DEFAULT_RETRY_MAX_INTERVAL;

    private int concurrency = DEFAULT_CONCURRENCY;

    private int failedConcurrency = DEFAULT_FAILED_CONCURRENCY;

    private String failedGroupId = DEFAULT_FAILED_GROUP_ID;

}