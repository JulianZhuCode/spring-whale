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
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    private TestableEventKafkaMessageConsumer consumer;
    private FilteringOrderListener filteringListener;
    private AcceptAllListener acceptAllListener;

    @BeforeEach
    void setUp() {
        filteringListener = new FilteringOrderListener();
        acceptAllListener = new AcceptAllListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of(
                "filteringOrderListener", filteringListener,
                "acceptAllListener", acceptAllListener
        );
        consumer = new TestableEventKafkaMessageConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, kafkaTemplate);
    }

    @Test
    @DisplayName("Should skip event when accept returns false")
    void testFilterRejectsEvent() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");
        event.setStatus("PENDING");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(filteringListener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertFalse(filteringListener.isInvoked());
        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should process event when accept returns true")
    void testFilterAcceptsEvent() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-002");
        event.setStatus("PAID");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(filteringListener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertTrue(filteringListener.isInvoked());
        assertNotNull(filteringListener.getReceivedEvent());
        assertEquals("ORDER-002", filteringListener.getReceivedEvent().getOrderId());
        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should accept all events when accept is not overridden")
    void testDefaultAcceptAll() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-003");
        event.setStatus("ANY");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(acceptAllListener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertTrue(acceptAllListener.isInvoked());
        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should filter independently per listener: one accepts, one rejects")
    void testFilterIndependently() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-004");
        event.setStatus("PAID");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(filteringListener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));

        assertTrue(filteringListener.isInvoked());
        assertTrue(ackCalled.get());
    }

    static class OrderCreatedEvent {
        private String orderId;
        private String status;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    static class FilteringOrderListener extends AbstractEventListener<OrderCreatedEvent> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);
        private OrderCreatedEvent receivedEvent;

        public FilteringOrderListener() {
            super(OrderCreatedEvent.class);
        }

        @Override
        public boolean accept(Object event) {
            OrderCreatedEvent order = (OrderCreatedEvent) event;
            return "PAID".equals(order.getStatus());
        }

        @Override
        public void doEvent(OrderCreatedEvent event, EventContext eventContext) {
            invoked.set(true);
            receivedEvent = event;
        }

        public boolean isInvoked() {
            return invoked.get();
        }

        public OrderCreatedEvent getReceivedEvent() {
            return receivedEvent;
        }
    }

    static class AcceptAllListener extends AbstractEventListener<OrderCreatedEvent> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        public AcceptAllListener() {
            super(OrderCreatedEvent.class);
        }

        @Override
        public void doEvent(OrderCreatedEvent event, EventContext eventContext) {
            invoked.set(true);
        }

        public boolean isInvoked() {
            return invoked.get();
        }
    }
}