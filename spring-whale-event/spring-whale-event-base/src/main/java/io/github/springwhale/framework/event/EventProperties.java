package io.github.springwhale.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.event")
public class EventProperties {

    private String defaultTopic = "DEFAULT_EVENT_TOPIC";

    private String failedTopic = "EVENT_FAILED_TOPIC";

    private String concurrency = "1";

    private int maxRetries = 3;

    private int retryInterval = 5;
}
