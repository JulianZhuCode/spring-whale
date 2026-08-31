package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.PublishOption;
import io.github.springwhale.framework.event.local.LocalEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class LocalEventPublisherTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventProperties eventProperties;

    private ApplicationEventPublisher applicationEventPublisher;
    private LocalEventPublisher publisher;

    static class OrderCreatedEvent {
        private String orderId;
        private String userId;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    static class PlainEvent {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        publisher = new LocalEventPublisher(eventProperties, objectMapper,
                Collections.emptyList(), applicationEventPublisher);
    }

    @Test
    @DisplayName("Should publish event via ApplicationEventPublisher")
    void testPublishEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");
        event.setUserId("USER-001");

        publisher.publish(event);

        verify(applicationEventPublisher, times(1)).publishEvent(any(EventMessage.class));
    }

    @Test
    @DisplayName("Should publish with PublishOption overrides")
    void testPublishWithOption() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-002");
        event.setUserId("USER-002");

        PublishOption option = PublishOption.builder()
                .topic("custom-topic")
                .businessName("custom.order")
                .build();

        publisher.publish(event, option);

        verify(applicationEventPublisher, times(1)).publishEvent(any(EventMessage.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when publishing null event")
    void testPublishNullEvent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> publisher.publish((Object) null));
        assertEquals("event must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when publishing null EventMessage")
    void testPublishNullEventMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> publisher.publish((EventMessage) null));
        assertEquals("message must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should publish EventMessage directly")
    void testPublishEventMessage() {
        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName("test.event");
        message.setTopic("test-topic");
        message.setData("{\"data\":\"test\"}");

        publisher.publish(message);

        verify(applicationEventPublisher, times(1)).publishEvent(message);
    }

    @Test
    @DisplayName("Should use default topic and class name when no annotation")
    void testPublishWithDefaults() {
        PlainEvent event = new PlainEvent();
        event.setData("test");

        assertDoesNotThrow(() -> publisher.publish(event));

        verify(applicationEventPublisher, times(1)).publishEvent(any(EventMessage.class));
    }
}