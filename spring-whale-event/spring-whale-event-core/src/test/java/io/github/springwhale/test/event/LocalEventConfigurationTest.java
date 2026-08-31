package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.local.LocalEventMessageConsumer;
import io.github.springwhale.framework.event.local.LocalEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class LocalEventConfigurationTest {

    @Autowired
    private LocalEventPublisher localEventPublisher;

    @Autowired
    private LocalEventMessageConsumer localEventMessageConsumer;

    @Autowired
    private EventProperties eventProperties;

    @Test
    @DisplayName("Should auto-configure LocalEventPublisher when mode is local")
    void testLocalEventPublisher() {
        assertNotNull(localEventPublisher);
    }

    @Test
    @DisplayName("Should auto-configure LocalEventMessageConsumer when mode is local")
    void testLocalEventMessageConsumer() {
        assertNotNull(localEventMessageConsumer);
    }

    @Test
    @DisplayName("Default mode should be local")
    void testDefaultMode() {
        assertEquals("local", eventProperties.getMode());
    }
}