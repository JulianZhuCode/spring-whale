package io.github.springwhale.framework.event.rabbit;

import com.rabbitmq.client.Channel;
import io.github.springwhale.framework.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * RabbitMQ implementation of {@link EventMessageConsumer}.
 * <p>Consumes messages from RabbitMQ queues with manual acknowledgment
 * and forwards failed messages to the failed topic for retry processing.</p>
 */
@Slf4j
public class RabbitEventMessageConsumer extends EventMessageConsumer {
    private final RabbitTemplate rabbitTemplate;

    public RabbitEventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                      List<EventMetricsCollector> metricsCollectors,
                                      Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                      RabbitTemplate rabbitTemplate) {
        super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap);
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Main RabbitMQ event listener.
     * <p>Consumes messages from the configured topic(s) with manual acknowledgment.
     * Topics are resolved from {@code eventProperties.consumerTopics} (comma-separated).
     * The outer catch block intentionally does NOT acknowledge the message, so that
     * RabbitMQ will re-deliver it — this is by design for at-least-once semantics.</p>
     */
    @RabbitListener(queues = "#{@eventProperties.consumerTopics.split(',')}",
            ackMode = "MANUAL_IMMEDIATE",
            concurrency = "#{@eventProperties.concurrency}")
    public void listener(String payload, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.debug("Consuming event message: {}", payload);
            EventContext context = EventContext.builder()
                    .timestamp(System.currentTimeMillis())
                    .topic(eventProperties.getConsumerTopics())
                    .build();
            consumeRawMessage(payload, context, () -> {
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to ack RabbitMQ message", e);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while consuming event message", e);
        }
    }

    @Override
    protected void sendToFailedTopic(EventMessage message) {
        try {
            rabbitTemplate.convertAndSend(eventProperties.getFailedTopic(), message.getBusinessName(),
                    jsonMapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new RuntimeException("send failed-event to RabbitMQ failed", e);
        }
    }
}