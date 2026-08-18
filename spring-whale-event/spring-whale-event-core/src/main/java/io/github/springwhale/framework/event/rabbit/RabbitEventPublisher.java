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
     * <p>Uses {@code businessName} as the routing key for explicit routing
     * from the exchange to the bound queue.</p>
     */
    @Override
    protected void doSend(EventMessage message) {
        rabbitTemplate.convertAndSend(message.getTopic(), message.getBusinessName(), jsonMapper.writeValueAsString(message));
    }

}