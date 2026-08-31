package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.BatchPublishException;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.PublishOption;
import io.github.springwhale.framework.event.rabbit.RabbitEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisher publisher;

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
        reset(rabbitTemplate);
        publisher = new RabbitEventPublisher(eventProperties, objectMapper,
                Collections.emptyList(), rabbitTemplate);
    }

    @Test
    @DisplayName("Should publish event via RabbitTemplate")
    void testPublishEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");
        event.setUserId("USER-001");

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should publish event with PublishOption overrides")
    void testPublishWithOption() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-002");
        event.setUserId("USER-002");

        PublishOption option = PublishOption.builder()
                .topic("custom-topic")
                .businessName("custom.order")
                .build();

        publisher.publish(event, option);

        verify(rabbitTemplate).convertAndSend(eq("custom-topic"), eq("custom.order"), anyString());
    }

    @Test
    @DisplayName("Should publish event without PublishOption using defaults")
    void testPublishWithoutOption() {
        PlainEvent event = new PlainEvent();
        event.setData("test-data");

        assertDoesNotThrow(() -> publisher.publish(event));

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should publish event with null PublishOption falling back to defaults")
    void testPublishWithNullOption() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-003");

        assertDoesNotThrow(() -> publisher.publish(event, null));

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should publish EventMessage directly")
    void testPublishEventMessage() {
        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName("test.business");
        message.setTopic("test-topic");
        message.setData("{\"key\":\"value\"}");

        assertDoesNotThrow(() -> publisher.publish(message));

        verify(rabbitTemplate).convertAndSend(eq("test-topic"), eq("test.business"), anyString());
    }

    @Test
    @DisplayName("Should throw RuntimeException when RabbitMQ send fails")
    void testPublishFailure() {
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());

        PlainEvent event = new PlainEvent();
        event.setData("test-data");

        assertThrows(RuntimeException.class, () -> publisher.publish(event));
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
    @DisplayName("Should use partitionKey as routing key when provided")
    void testPartitionKeyUsedAsRoutingKey() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-004");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .partitionKey("ORDER-004")
                .build();

        publisher.publish(event, option);

        verify(rabbitTemplate).convertAndSend(eq("order-topic"), eq("ORDER-004"), anyString());
    }

    @Test
    @DisplayName("Should use businessName as routing key when partitionKey is null")
    void testNullPartitionKeyFallsBackToBusinessName() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-005");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .partitionKey(null)
                .build();

        publisher.publish(event, option);

        verify(rabbitTemplate).convertAndSend(eq("order-topic"), eq("order.created"), anyString());
    }

    @Test
    @DisplayName("Should use businessName as routing key when partitionKey is not set")
    void testNoPartitionKeyFallsBackToBusinessName() {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-006");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .build();

        publisher.publish(event, option);

        verify(rabbitTemplate).convertAndSend(eq("order-topic"), eq("order.created"), anyString());
    }

    @Test
    @DisplayName("Should use same routing key for all events in batch with partitionKey")
    void testBatchPublishWithPartitionKey() throws BatchPublishException {
        OrderCreatedEvent event1 = new OrderCreatedEvent();
        event1.setOrderId("ORDER-007");
        OrderCreatedEvent event2 = new OrderCreatedEvent();
        event2.setOrderId("ORDER-008");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .partitionKey("SHARED-KEY")
                .build();

        publisher.publishBatch(List.of(event1, event2), option);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), routingKeyCaptor.capture(), anyString());
        for (String key : routingKeyCaptor.getAllValues()) {
            assertEquals("SHARED-KEY", key);
        }
    }

    @Test
    @DisplayName("Should publish multiple events without error")
    void testPublishMultipleEvents() {
        OrderCreatedEvent event1 = new OrderCreatedEvent();
        OrderCreatedEvent event2 = new OrderCreatedEvent();

        assertDoesNotThrow(() -> publisher.publish(event1));
        assertDoesNotThrow(() -> publisher.publish(event2));

        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), anyString());
    }
}