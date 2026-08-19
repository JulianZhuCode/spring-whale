package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.PublishOption;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

@SpringBootTest
@Import(TestEventConfiguration.class)
class KafkaEventPublisherTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventProperties eventProperties;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaEventPublisher publisher;

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
        Mockito.reset(kafkaTemplate);
        publisher = new KafkaEventPublisher(eventProperties, objectMapper,
                Collections.emptyList(), kafkaTemplate);
    }

    @Test
    @DisplayName("Should publish event with PublishOption overrides")
    void testPublishWithOption() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");
        event.setUserId("USER-001");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .build();

        assertDoesNotThrow(() -> publisher.publish(event, option));
    }

    @Test
    @DisplayName("Should publish event without PublishOption using defaults")
    void testPublishWithoutOption() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        PlainEvent event = new PlainEvent();
        event.setData("test-data");

        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    @DisplayName("Should publish event with custom topic and business name")
    void testPublishWithCustomOverrides() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-002");
        event.setUserId("USER-002");

        PublishOption option = PublishOption.builder()
                .topic("custom-topic")
                .businessName("custom.business")
                .build();

        assertDoesNotThrow(() -> publisher.publish(event, option));
    }

    @Test
    @DisplayName("Should publish event with null PublishOption falling back to defaults")
    void testPublishWithNullOption() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-003");

        assertDoesNotThrow(() -> publisher.publish(event, null));
    }

    @Test
    @DisplayName("Should publish EventMessage directly")
    void testPublishEventMessage() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName("test.business");
        message.setTopic("test-topic");
        message.setData("{\"key\":\"value\"}");

        assertDoesNotThrow(() -> publisher.publish(message));
    }

    @Test
    @DisplayName("Should throw RuntimeException when Kafka send fails")
    void testPublishFailure() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        PlainEvent event = new PlainEvent();
        event.setData("test-data");

        assertThrows(RuntimeException.class, () -> publisher.publish(event));
    }

    @Test
    @DisplayName("Should publish multiple events without error")
    void testPublishMultipleEvents() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        OrderCreatedEvent event1 = new OrderCreatedEvent();
        OrderCreatedEvent event2 = new OrderCreatedEvent();

        assertDoesNotThrow(() -> publisher.publish(event1));
        assertDoesNotThrow(() -> publisher.publish(event2));
    }
}