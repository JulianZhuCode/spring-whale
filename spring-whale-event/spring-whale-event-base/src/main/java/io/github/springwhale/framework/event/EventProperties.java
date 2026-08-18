package io.github.springwhale.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.event")
public class EventProperties {

    public static final String DEFAULT_EVENT_TOPIC = "EVENT_TOPIC";
    public static final String DEFAULT_FAILED_TOPIC = "EVENT_FAILED_TOPIC";
    public static final String DEFAULT_CONCURRENCY = "1";
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_RETRY_INTERVAL = 5;
    public static final int DEFAULT_RETRY_BATCH_SIZE = 1000;
    public static final int DEFAULT_RETRY_SCHEDULE_INTERVAL = 60000;
    public static final int DEFAULT_SEND_TIMEOUT_SECONDS = 3;

    private String eventTopic = DEFAULT_EVENT_TOPIC;

    private String failedTopic = DEFAULT_FAILED_TOPIC;

    private String concurrency = DEFAULT_CONCURRENCY;

    private int maxRetries = DEFAULT_MAX_RETRIES;

    private int retryInterval = DEFAULT_RETRY_INTERVAL;

    private int retryBatchSize = DEFAULT_RETRY_BATCH_SIZE;

    private int retryScheduleInterval = DEFAULT_RETRY_SCHEDULE_INTERVAL;

    private int sendTimeoutSeconds = DEFAULT_SEND_TIMEOUT_SECONDS;

}