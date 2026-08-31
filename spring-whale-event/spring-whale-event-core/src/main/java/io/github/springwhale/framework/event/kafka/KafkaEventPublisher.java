package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class KafkaEventPublisher extends EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                               List<EventMetricsCollector> metricsCollectors,
                               KafkaTemplate<String, String> kafkaTemplate) {
        super(properties, jsonMapper, metricsCollectors);
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send the event message to Kafka synchronously with a configurable timeout.
     * <p>Synchronous send is used intentionally: the caller needs to know whether the
     * message was accepted by Kafka before proceeding. The timeout is bounded by
     * {@code sendTimeoutSeconds} (default 3s) to prevent indefinite blocking.</p>
     * <p>When {@code partitionKey} is provided, it is used as the Kafka message key
     * to guarantee that all events with the same key are written to the same partition
     * in order. When null, the message ID is used as the key.</p>
     */
    @Override
    protected void doSend(EventMessage message, String partitionKey) throws Exception {
        String key = partitionKey != null ? partitionKey : message.getId();
        kafkaTemplate.send(message.getTopic(), key, jsonMapper.writeValueAsString(message))
                .get(properties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
    }

}