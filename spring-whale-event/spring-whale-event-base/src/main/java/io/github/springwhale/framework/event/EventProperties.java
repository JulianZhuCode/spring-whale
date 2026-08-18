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

    private String eventTopic = DEFAULT_EVENT_TOPIC;

    private String failedTopic = DEFAULT_FAILED_TOPIC;

    private String concurrency = DEFAULT_CONCURRENCY;

    private int maxRetries = 3;

    private int retryInterval = 5;
}
