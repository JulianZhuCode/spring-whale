package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.event.Event;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.PublishOption;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher extends EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(@Valid Object event) {
        Assert.notNull(event, "event must not be null");
        Event eventAnnotation = findEventAnnotation(event);
        String businessName = buildBusinessName(event, eventAnnotation);
        String topic = buildTopic(eventAnnotation);
        send(event, businessName, topic);

    }

    @Override
    public void publish(@Valid Object event, PublishOption option) {
        Assert.notNull(event, "event must not be null");
        if (option == null) {
            publish(event);
            return;
        }
        String businessName = option.businessName();
        String topic = option.topic();
        if (StringUtils.hasText(businessName) && StringUtils.hasText(topic)) {
            send(event, businessName, topic);
            return;
        }
        Event eventAnnotation = findEventAnnotation(event);
        if (!StringUtils.hasText(businessName)) {
            businessName = buildBusinessName(event, eventAnnotation);
        }
        if (!StringUtils.hasText(topic)) {
            topic = buildTopic(eventAnnotation);
        }
        send(event, businessName, topic);
    }

    @Override
    public void publish(@Valid EventMessage message) {
        Assert.notNull(message, "message must not be null");
        send(message);
    }

    private void send(Object event, String businessName, String topic) {
        EventMessage message = buildEventMessage(event, businessName, topic);
        send(message);
    }

    /**
     * Send the event message to Kafka synchronously with a configurable timeout.
     * <p>Synchronous send is used intentionally: the caller needs to know whether the
     * message was accepted by Kafka before proceeding. The timeout is bounded by
     * {@code sendTimeoutSeconds} (default 3s) to prevent indefinite blocking.
     * For fire-and-forget scenarios, consider using KafkaTemplate's async send.</p>
     *
     * @param message the event message to send
     * @throws RuntimeException if the send fails or times out
     */
    private void send(EventMessage message) {
        try {
            kafkaTemplate.send(message.getTopic(), message.getId(), jsonMapper.writeValueAsString(message)).get(properties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("send event to Kafka failed", e);
        }
    }

}