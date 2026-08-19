package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.kafka.EventKafkaMessageConsumer;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class TestableEventKafkaMessageConsumer extends EventKafkaMessageConsumer {

    public TestableEventKafkaMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                             List<EventMetricsCollector> metricsCollectors,
                                             Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                             KafkaTemplate<String, String> kafkaTemplate) {
        super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, kafkaTemplate);
    }

    @Override
    public void consumeRawMessage(String rawPayload, EventContext context, Runnable onSuccess) {
        super.consumeRawMessage(rawPayload, context, onSuccess);
    }
}