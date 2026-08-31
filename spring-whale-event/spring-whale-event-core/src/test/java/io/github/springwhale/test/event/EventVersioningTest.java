package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.*;
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
class EventVersioningTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    private TestableEventKafkaMessageConsumer consumer;
    private V1Listener v1Listener;
    private V2Listener v2Listener;
    private MultiVersionListener multiVersionListener;

    @Event(value = "V1Event", version = 1)
    static class V1Event {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    @Event(value = "V2Event", version = 2)
    static class V2Event {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    static class UnannotatedEvent {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    static class V1Listener extends AbstractEventListener<V1Event> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        public V1Listener() {
            super(V1Event.class);
        }

        @Override
        public void doEvent(V1Event event, EventContext eventContext) {
            invoked.set(true);
        }

        public boolean isInvoked() { return invoked.get(); }
    }

    static class V2Listener extends AbstractEventListener<V2Event> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        public V2Listener() {
            super(V2Event.class);
        }

        @Override
        public void doEvent(V2Event event, EventContext eventContext) {
            invoked.set(true);
        }

        public boolean isInvoked() { return invoked.get(); }
    }

    static class MultiVersionListener extends AbstractEventListener<V2Event> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        public MultiVersionListener() {
            super(V2Event.class);
        }

        @Override
        public int[] supportedVersions() {
            return new int[] { 1, 2 };
        }

        @Override
        public void doEvent(V2Event event, EventContext eventContext) {
            invoked.set(true);
        }

        public boolean isInvoked() { return invoked.get(); }
    }

    static class UnannotatedListener extends AbstractEventListener<UnannotatedEvent> {
        private final AtomicBoolean invoked = new AtomicBoolean(false);

        public UnannotatedListener() {
            super(UnannotatedEvent.class);
        }

        @Override
        public void doEvent(UnannotatedEvent event, EventContext eventContext) {
            invoked.set(true);
        }

        public boolean isInvoked() { return invoked.get(); }
    }

    @BeforeEach
    void setUp() {
        v1Listener = new V1Listener();
        v2Listener = new V2Listener();
        multiVersionListener = new MultiVersionListener();
    }

    private TestableEventKafkaMessageConsumer createConsumerWith(AbstractEventListener<?>... listeners) {
        Map<String, AbstractEventListener<?>> listenerMap = new java.util.HashMap<>();
        for (int i = 0; i < listeners.length; i++) {
            listenerMap.put("listener" + i, listeners[i]);
        }
        return new TestableEventKafkaMessageConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listenerMap, kafkaTemplate);
    }

    private void assertEventProcessed(TestableEventKafkaMessageConsumer c, String businessName,
                                       Object event, Integer version) throws Exception {
        EventMessage message = new EventMessage();
        message.setSource("test-service");
        message.setBusinessName(businessName);
        message.setTopic("test-topic");
        message.setVersion(version);
        message.setData(objectMapper.writeValueAsString(event));

        String rawPayload = objectMapper.writeValueAsString(message);
        EventContext context = EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();

        AtomicBoolean ackCalled = new AtomicBoolean(false);
        c.consumeRawMessage(rawPayload, context, () -> ackCalled.set(true));
        assertTrue(ackCalled.get());
    }

    @Test
    @DisplayName("Should accept event when version matches listener's supported version")
    void testVersionMatches() throws Exception {
        consumer = createConsumerWith(v1Listener);
        V1Event event = new V1Event();
        event.setData("test");

        assertEventProcessed(consumer, v1Listener.businessName(), event, 1);
        assertTrue(v1Listener.isInvoked());
    }

    @Test
    @DisplayName("Should skip event when version does not match listener's supported version")
    void testVersionMismatch() throws Exception {
        consumer = createConsumerWith(v1Listener);
        V1Event event = new V1Event();
        event.setData("test");

        assertEventProcessed(consumer, v1Listener.businessName(), event, 2);
        assertFalse(v1Listener.isInvoked());
    }

    @Test
    @DisplayName("Should treat null version as version 1 for backward compatibility")
    void testNullVersionTreatedAsDefault() throws Exception {
        consumer = createConsumerWith(v1Listener);
        V1Event event = new V1Event();
        event.setData("test");

        assertEventProcessed(consumer, v1Listener.businessName(), event, null);
        assertTrue(v1Listener.isInvoked());
    }

    @Test
    @DisplayName("Should skip null-version event when listener supports only v2")
    void testNullVersionSkipWhenListenerOnlySupportsV2() throws Exception {
        consumer = createConsumerWith(v2Listener);
        V2Event event = new V2Event();
        event.setData("test");

        assertEventProcessed(consumer, v2Listener.businessName(), event, null);
        assertFalse(v2Listener.isInvoked());
    }

    @Test
    @DisplayName("Should accept v2 event when listener supports v2")
    void testV2ListenerAcceptsV2() throws Exception {
        consumer = createConsumerWith(v2Listener);
        V2Event event = new V2Event();
        event.setData("test");

        assertEventProcessed(consumer, v2Listener.businessName(), event, 2);
        assertTrue(v2Listener.isInvoked());
    }

    @Test
    @DisplayName("Should accept events of multiple versions when listener overrides supportedVersions")
    void testMultiVersionListenerAcceptsV1() throws Exception {
        consumer = createConsumerWith(multiVersionListener);
        V2Event event = new V2Event();
        event.setData("test");

        assertEventProcessed(consumer, multiVersionListener.businessName(), event, 1);
        assertTrue(multiVersionListener.isInvoked());
    }

    @Test
    @DisplayName("Should accept v2 event with multi-version listener")
    void testMultiVersionListenerAcceptsV2() throws Exception {
        // Reset state from previous test
        MultiVersionListener fresh = new MultiVersionListener();
        consumer = createConsumerWith(fresh);

        V2Event event = new V2Event();
        event.setData("test");

        assertEventProcessed(consumer, fresh.businessName(), event, 2);
        assertTrue(fresh.isInvoked());
    }

    @Test
    @DisplayName("Should default to version 1 for unannotated event class")
    void testUnannotatedEventDefaultsToV1() throws Exception {
        UnannotatedListener listener = new UnannotatedListener();
        consumer = createConsumerWith(listener);
        UnannotatedEvent event = new UnannotatedEvent();
        event.setData("test");

        assertEventProcessed(consumer, listener.businessName(), event, 1);
        assertTrue(listener.isInvoked());
    }

    @Test
    @DisplayName("Should skip unannotated event when listener only supports v2")
    void testUnannotatedEventSkipForV2() throws Exception {
        // Override a V2Listener to be associated with the unannotated event's business name
        UnannotatedListener listener = new UnannotatedListener();
        consumer = createConsumerWith(listener);
        UnannotatedEvent event = new UnannotatedEvent();
        event.setData("test");

        assertEventProcessed(consumer, listener.businessName(), event, 2);
        assertFalse(listener.isInvoked());
    }

    @Test
    @DisplayName("supportedVersions returns annotation version when Event annotation is present")
    void testDefaultSupportedVersionsFromAnnotation() {
        V2Listener listener = new V2Listener();
        int[] versions = listener.supportedVersions();
        assertArrayEquals(new int[] { 2 }, versions);
    }

    @Test
    @DisplayName("supportedVersions defaults to {1} when no Event annotation is present")
    void testDefaultSupportedVersionsNoAnnotation() {
        UnannotatedListener listener = new UnannotatedListener();
        int[] versions = listener.supportedVersions();
        assertArrayEquals(new int[] { Event.DEFAULT_VERSION }, versions);
    }
}