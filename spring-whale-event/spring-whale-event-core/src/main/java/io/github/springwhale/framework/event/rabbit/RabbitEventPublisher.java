package io.github.springwhale.framework.event.rabbit;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public class RabbitEventPublisher extends EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                                List<EventMetricsCollector> metricsCollectors,
                                RabbitTemplate rabbitTemplate) {
        super(properties, jsonMapper, metricsCollectors);
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send the event message to RabbitMQ using {@code convertAndSend}.
     * <p>When {@code partitionKey} is provided, it is used as the routing key
     * to guarantee that all events with the same key are routed to the same
     * queue in order. When null, the {@code businessName} is used as the routing key.</p>
     */
    @Override
    protected void doSend(EventMessage message, String partitionKey) {
        String routingKey = partitionKey != null ? partitionKey : message.getBusinessName();
        rabbitTemplate.convertAndSend(message.getTopic(), routingKey, jsonMapper.writeValueAsString(message));
    }

}