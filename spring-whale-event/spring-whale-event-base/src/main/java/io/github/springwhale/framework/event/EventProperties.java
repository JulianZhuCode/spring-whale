package io.github.springwhale.framework.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.event")
public class EventProperties {

    private String defaultTopic = "EVENT_TOPIC";

    private String retryTopic = "EVENT_RETRY_TOPIC";

    private String errorTopic = "EVENT_ERROR_TOPIC";
}
