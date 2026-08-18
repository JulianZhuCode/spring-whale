package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMessageConsumer;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EventKafkaMessageConsumer extends EventMessageConsumer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventKafkaMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                     List<EventMetricsCollector> metricsCollectors,
                                     Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                     KafkaTemplate<String, String> kafkaTemplate) {
        super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap);
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Main Kafka event listener.
     * <p>Consumes messages from the configured topic(s) with manual acknowledgment.
     * The outer catch block intentionally does NOT acknowledge the message, so that
     * Kafka will re-deliver it — this is by design for at-least-once semantics.</p>
     */
    @KafkaListener(topics = "#{@eventProperties.consumerTopics.split(',')}",
            concurrency = "#{@eventProperties.concurrency}",
            groupId = "#{spring.application.name}",
            properties = {"#{'auto.offset.reset:' + @kafkaEventProperties.autoOffsetReset}"})
    public void listener(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.debug("Consuming event message: {}", record.value());
            EventContext context = EventContext.builder()
                    .timestamp(record.timestamp())
                    .topic(record.topic())
                    .build();
            consumeRawMessage(record.value(), context, ack::acknowledge);
        } catch (Exception e) {
            // Intentionally do NOT acknowledge here: at-least-once semantics.
            // If processing fails (e.g. database or downstream unavailable),
            // Kafka will re-deliver the message once the system recovers.
            log.error("Exception occurred while consuming event message", e);
        }
    }

    /**
     * Send the message to the Kafka failed topic synchronously.
     * <p>Blocking send with bounded timeout is intentional: the caller must confirm
     * the message is persisted before acknowledging the original message.</p>
     */
    @Override
    protected void sendToFailedTopic(EventMessage message) {
        try {
            kafkaTemplate.send(eventProperties.getFailedTopic(), message.getId(), jsonMapper.writeValueAsString(message))
                    .get(eventProperties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("send failed-event to Kafka failed", e);
        }
    }
}