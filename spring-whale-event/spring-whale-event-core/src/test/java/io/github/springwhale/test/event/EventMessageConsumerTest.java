package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventMessageConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

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

    private TestableEventKafkaMessageConsumer consumer;
    private OrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderCreatedListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("orderCreatedListener", listener);
        consumer = new TestableEventKafkaMessageConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, kafkaTemplate);
    }

    @Test
    @DisplayName("Should consume valid raw message and dispatch to listener")
    void testConsumeValidRawMessage() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(listener.getBusinessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertTrue(listener.isInvoked());
        assertNotNull(listener.getReceivedEvent());
        assertEquals("ORDER-001", listener.getReceivedEvent().getOrderId());
        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should ack when raw payload is null")
    void testConsumeNullPayload() {
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(null, context, () -> ackCalled.set(true));

        assertTrue(ackCalled.get());
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should ack when raw payload is invalid JSON")
    void testConsumeInvalidJson() {
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage("not-valid-json", context, () -> ackCalled.set(true));

        assertTrue(ackCalled.get());
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should ack when listener map is empty")
    void testConsumeWithEmptyListeners() {
        TestableEventKafkaMessageConsumer emptyConsumer = new TestableEventKafkaMessageConsumer(objectMapper,
                eventProperties, Collections.emptyList(), Collections.emptyMap(), kafkaTemplate);

        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        emptyConsumer.consumeRawMessage("{}", context, () -> ackCalled.set(true));

        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should not dispatch when no matching listener for business name")
    void testNoMatchingListener() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-002");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName("unknown.business");
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertTrue(ackCalled.get());
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should add and remove listeners at runtime")
    void testAddRemoveListener() {
        assertFalse(consumer.listenerIsEmpty());

        OrderCreatedListener newListener = new OrderCreatedListener();
        consumer.addListener("newListener", newListener);

        assertTrue(consumer.getListenerNameToInstanceMap().containsKey("newListener"));
        assertTrue(consumer.getListenerInstanceToNameMap().containsKey(newListener));

        consumer.removeListener("newListener");

        assertFalse(consumer.getListenerNameToInstanceMap().containsKey("newListener"));
    }

    @Test
    @DisplayName("Should throw when adding duplicate listener instance")
    void testAddDuplicateListener() {
        assertThrows(IllegalStateException.class, () ->
                consumer.addListener("duplicateName", listener));
    }

    @Test
    @DisplayName("Should refresh listeners")
    void testRefreshListeners() {
        assertDoesNotThrow(() -> consumer.refreshListeners());
        assertFalse(consumer.listenerIsEmpty());
    }
}