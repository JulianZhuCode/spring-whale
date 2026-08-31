package io.github.springwhale.framework.event;

import lombok.Getter;

/**
 * Exception thrown when an event publish operation fails.
 * <p>Wraps the original cause (e.g. Kafka/RabbitMQ send failure, serialization error)
 * so callers can inspect and handle the specific failure type.</p>
 */
@Getter
public class EventPublishException extends RuntimeException {

    private final String topic;
    private final String businessName;

    public EventPublishException(String topic, String businessName, Throwable cause) {
        super("Failed to publish event [topic=" + topic + ", businessName=" + businessName + "]", cause);
        this.topic = topic;
        this.businessName = businessName;
    }

}