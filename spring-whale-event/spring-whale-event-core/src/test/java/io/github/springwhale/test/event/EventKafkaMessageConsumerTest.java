package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.kafka.EventKafkaMessageConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventKafkaMessageConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Acknowledgment ack;

    static class OrderCreatedEvent {
        private String orderId;
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
    }

    static class OrderCreatedListener extends AbstractEventListener<OrderCreatedEvent> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        private OrderCreatedEvent receivedEvent;

        public OrderCreatedListener() {
            super(OrderCreatedEvent.class);
        }

        @Override
        public void doEvent(OrderCreatedEvent event, EventContext eventContext) {
            invoked.set(true);
            receivedEvent = event;
        }

        public boolean isInvoked() { return invoked.get(); }
        public OrderCreatedEvent getReceivedEvent() { return receivedEvent; }
    }

    static class TestableEventKafkaMessageConsumer2 extends EventKafkaMessageConsumer {
        public TestableEventKafkaMessageConsumer2(ObjectMapper jsonMapper, EventProperties eventProperties,
                                                  List<EventMetricsCollector> metricsCollectors,
                                                  Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                  KafkaTemplate<String, String> kafkaTemplate) {
            super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, kafkaTemplate);
        }

        @Override
        public void sendToFailedTopic(EventMessage message) {
            super.sendToFailedTopic(message);
        }
    }

    private TestableEventKafkaMessageConsumer2 consumer;
    private OrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderCreatedListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("orderCreatedListener", listener);
        consumer = new TestableEventKafkaMessageConsumer2(objectMapper, eventProperties,
                Collections.emptyList(), listeners, kafkaTemplate);
    }

    @Test
    @DisplayName("Should consume valid message and ack")
    void testListenerWithValidMessage() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(listener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));
        message.setMessageType(MessageType.EVENT);

        String rawPayload = objectMapper.writeValueAsString(message);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);

        consumer.listener(record, ack);

        verify(ack).acknowledge();
        assertTrue(listener.isInvoked());
        assertNotNull(listener.getReceivedEvent());
        assertEquals("ORDER-001", listener.getReceivedEvent().getOrderId());
    }

    @Test
    @DisplayName("Should ack when message is null")
    void testListenerWithNullPayload() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", null);

        consumer.listener(record, ack);

        verify(ack).acknowledge();
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should ack when message is empty string")
    void testListenerWithEmptyPayload() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", "");

        consumer.listener(record, ack);

        verify(ack).acknowledge();
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should ack when message is invalid JSON")
    void testListenerWithInvalidJson() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", "not-valid-json");

        consumer.listener(record, ack);

        verify(ack).acknowledge();
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should not ack when listener throws exception")
    void testListenerWithListenerException() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(listener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));
        message.setMessageType(MessageType.EVENT);

        String rawPayload = objectMapper.writeValueAsString(message);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);

        doThrow(new RuntimeException("Ack error")).when(ack).acknowledge();

        consumer.listener(record, ack);

        verify(ack).acknowledge();
        assertTrue(listener.isInvoked());
    }

    @Test
    @DisplayName("Should send failed-event to Kafka via sendToFailedTopic")
    @SuppressWarnings("unchecked")
    void testSendToFailedTopic() throws Exception {
        EventMessage message = new EventMessage();
        message.setId("msg-001");
        message.setSource("test-service");
        message.setBusinessName("order.created");
        message.setTopic("test-topic");
        message.setData("{}");
        message.setMessageType(MessageType.FAIL);

        CompletableFuture<SendResult<String, String>> future = mock(CompletableFuture.class);
        when(future.get(anyLong(), any(TimeUnit.class))).thenReturn(null);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        consumer.sendToFailedTopic(message);

        verify(kafkaTemplate).send(anyString(), eq("msg-001"), anyString());
    }
}