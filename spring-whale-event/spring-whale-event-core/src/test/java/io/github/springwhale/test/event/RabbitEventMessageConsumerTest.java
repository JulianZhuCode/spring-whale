package io.github.springwhale.test.event;

import com.rabbitmq.client.Channel;
import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.rabbit.RabbitEventMessageConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitEventMessageConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Channel channel;

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

    static class TestableRabbitEventMessageConsumer extends RabbitEventMessageConsumer {
        public TestableRabbitEventMessageConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                                                  List<EventMetricsCollector> metricsCollectors,
                                                  Map<String, AbstractEventListener<?>> springListenerBeanMap,
                                                  RabbitTemplate rabbitTemplate) {
            super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, rabbitTemplate);
        }

        @Override
        public void sendToFailedTopic(EventMessage message) {
            super.sendToFailedTopic(message);
        }
    }

    private TestableRabbitEventMessageConsumer consumer;
    private OrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrderCreatedListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("orderCreatedListener", listener);
        consumer = new TestableRabbitEventMessageConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, rabbitTemplate);
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

        consumer.listener(rawPayload, channel, 1L);

        verify(channel).basicAck(1L, false);
        assertTrue(listener.isInvoked());
        assertNotNull(listener.getReceivedEvent());
        assertEquals("ORDER-001", listener.getReceivedEvent().getOrderId());
    }

    @Test
    @DisplayName("Should ack when message is null")
    void testListenerWithNullPayload() throws IOException {
        consumer.listener(null, channel, 1L);

        verify(channel).basicAck(1L, false);
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("Should ack when message is invalid JSON")
    void testListenerWithInvalidJson() throws IOException {
        consumer.listener("not-valid-json", channel, 1L);

        verify(channel).basicAck(1L, false);
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

        doThrow(new RuntimeException("Listener error")).when(channel).basicAck(anyLong(), anyBoolean());

        consumer.listener(rawPayload, channel, 1L);

        verify(channel).basicAck(1L, false);
        assertTrue(listener.isInvoked());
    }

    @Test
    @DisplayName("Should send failed-event to RabbitMQ via sendToFailedTopic")
    void testSendToFailedTopic() throws Exception {
        EventMessage message = new EventMessage();
        message.setId("msg-001");
        message.setSource("test-service");
        message.setBusinessName("order.created");
        message.setTopic("test-topic");
        message.setData("{}");
        message.setMessageType(MessageType.FAIL);

        consumer.sendToFailedTopic(message);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("order.created"), anyString());
    }
}