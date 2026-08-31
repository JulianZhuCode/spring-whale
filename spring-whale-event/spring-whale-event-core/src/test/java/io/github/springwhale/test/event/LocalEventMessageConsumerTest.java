package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.local.LocalEventMessageConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalEventMessageConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

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

    static class TestableLocalEventMessageConsumer extends LocalEventMessageConsumer {
        public TestableLocalEventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                                 List<EventMetricsCollector> metricsCollectors,
                                                 Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                 ApplicationEventPublisher publisher) {
            super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, publisher);
        }

        @Override
        public void sendToFailedTopic(EventMessage message) {
            super.sendToFailedTopic(message);
        }
    }

    private TestableLocalEventMessageConsumer consumer;
    private OrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderCreatedListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("orderCreatedListener", listener);
        consumer = new TestableLocalEventMessageConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, applicationEventPublisher);
    }

    @Test
    @DisplayName("Should dispatch EVENT message to matching listener")
    void testOnLocalEventWithValidEventMessage() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId("ORDER-001");

        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(listener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(event));
        message.setMessageType(MessageType.EVENT);

        consumer.onLocalEvent(message);

        assertTrue(listener.isInvoked());
        assertNotNull(listener.getReceivedEvent());
        assertEquals("ORDER-001", listener.getReceivedEvent().getOrderId());
    }

    @Test
    @DisplayName("Should not dispatch FAIL message type")
    void testOnLocalEventWithFailMessageType() {
        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(listener.businessName());
        message.setTopic("test-topic");
        message.setData("{}");
        message.setMessageType(MessageType.FAIL);

        consumer.onLocalEvent(message);

        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should not throw when no matching listener")
    void testOnLocalEventWithNoMatchingListener() {
        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName("unknown.business");
        message.setTopic("test-topic");
        message.setData("{}");
        message.setMessageType(MessageType.EVENT);

        assertDoesNotThrow(() -> consumer.onLocalEvent(message));
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should publish event via ApplicationEventPublisher on sendToFailedTopic")
    void testSendToFailedTopic() throws Exception {
        EventMessage message = new EventMessage();
        message.setId("msg-001");
        message.setSource("test-service");
        message.setBusinessName("order.created");
        message.setTopic("test-topic");
        message.setData("{}");
        message.setMessageType(MessageType.FAIL);

        consumer.sendToFailedTopic(message);

        ArgumentCaptor<EventMessage> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertEquals("msg-001", captor.getValue().getId());
        assertEquals(MessageType.FAIL, captor.getValue().getMessageType());
    }
}