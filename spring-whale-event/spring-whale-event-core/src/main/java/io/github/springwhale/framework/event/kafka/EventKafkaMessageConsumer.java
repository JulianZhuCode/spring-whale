package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.core.JacksonException;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class EventKafkaMessageConsumer extends EventMessageConsumer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static EventContext buildEventContext(ConsumerRecord<String, String> record, EventMessage message) {
        return EventContext.builder()
                .timestamp(record.timestamp())
                .topic(record.topic())
                .authenticationContext(message.getAuthenticationContext())
                .build();
    }

    /**
     * Main Kafka event listener.
     * <p>Consumes messages from the configured topic(s) with manual acknowledgment.
     * The outer catch block intentionally does NOT acknowledge the message, so that
     * Kafka will re-deliver it — this is by design for at-least-once semantics.
     * The only truly unrecoverable scenario (deserialization failure) is handled
     * in the inner catch block where the message is acknowledged and discarded.</p>
     */
    @KafkaListener(topics = "#{@eventProperties.listener.split(',')}",
            concurrency = "#{@kafkaEventProperties.concurrency}",
            groupId = "${spring.application.name}",
            properties = {"#{'auto.offset.reset:' + @kafkaEventProperties.autoOffsetReset}"})
    public void listener(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.debug("Consuming event message: {}", record.value());
            if (listenerIsEmpty()) {
                ack.acknowledge();
                return;
            }
            if (record.value() == null) {
                ack.acknowledge();
                return;
            }
            EventMessage message;
            try {
                message = jsonMapper.readValue(record.value(), EventMessage.class);
            } catch (JacksonException e) {
                log.error("Failed to deserialize event message: {}", record.value(), e);
                ack.acknowledge();
                return;
            }
            EventContext context = buildEventContext(record, message);
            handleMessage(message, context);
            ack.acknowledge();
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