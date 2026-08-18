package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class KafkaEventPublisher extends EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send the event message to Kafka synchronously with a configurable timeout.
     * <p>Synchronous send is used intentionally: the caller needs to know whether the
     * message was accepted by Kafka before proceeding. The timeout is bounded by
     * {@code sendTimeoutSeconds} (default 3s) to prevent indefinite blocking.</p>
     */
    @Override
    protected void doSend(EventMessage message) throws Exception {
        kafkaTemplate.send(message.getTopic(), message.getId(), jsonMapper.writeValueAsString(message))
                .get(properties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
    }

}