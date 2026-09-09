package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the dedup SPI integration (idempotency marking + per-message lock).
 * <p>Semantics under test (approved design): the invocation lives in
 * {@link AbstractEventListener} - the default implementation delegates to the
 * globally unique {@link EventDedupHandler} (fail-open), and listeners may
 * override {@code isProcessed}/{@code markProcessed}/{@code markFailed} for
 * per-listener customization. RETRY duplicates re-emit RETRY_SUCCESS so the
 * failed record converges to REPLAY_SUCCESS instead of being stuck in PENDING_RETRY.
 * The lock serializes concurrent deliveries of the same message id.</p>
 */
class EventDedupHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    private TestableConsumer consumer;
    private OrderCreatedListener listener;
    private RecordingIdempotencyHandler handler;

    @BeforeEach
    void setUp() {
        listener = new OrderCreatedListener();
        handler = new RecordingIdempotencyHandler();
        Map<String, AbstractEventListener<?>> listeners = Map.of("orderCreatedListener", listener);
        consumer = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, handler);
    }

    @Test
    @DisplayName("Should skip duplicate EVENT message and still ack")
    void testDuplicateEventSkipped() throws Exception {
        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-001"));
        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations());
        assertEquals(1, handler.markedProcessed());
        assertTrue(ack.get());

        // Second delivery of the same message: isProcessed=true -> skip, still ack.
        AtomicBoolean ack2 = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, ctx(), () -> ack2.set(true));

        assertEquals(1, listener.invocations(), "listener must not execute again");
        assertTrue(ack2.get());
        assertTrue(consumer.failedMessages.isEmpty(), "duplicate EVENT must not emit RETRY_SUCCESS");
    }

    @Test
    @DisplayName("Should re-emit RETRY_SUCCESS for duplicate RETRY message (convergence)")
    void testDuplicateRetryReEmitsRetrySuccess() throws Exception {
        EventMessage message = retryMessage("msg-002");
        // Simulate: the RETRY already succeeded but its completion signal was lost.
        handler.seedProcessed("msg-002", "orderCreatedListener");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(0, listener.invocations(), "listener must not execute again");
        assertEquals(1, consumer.failedMessages.size(), "RETRY_SUCCESS must be re-emitted");
        assertEquals(MessageType.RETRY_SUCCESS, consumer.failedMessages.get(0).getMessageType());
        assertEquals("msg-002", consumer.failedMessages.get(0).getId());
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should process RETRY message normally when not marked as processed")
    void testRetryNotBlockedWhenNotProcessed() throws Exception {
        EventMessage message = retryMessage("msg-003");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations());
        assertTrue(handler.isMarked("msg-003", "orderCreatedListener"));
        // Normal RETRY success flow: RETRY_SUCCESS emitted after successful execution.
        assertEquals(1, consumer.failedMessages.size());
        assertEquals(MessageType.RETRY_SUCCESS, consumer.failedMessages.get(0).getMessageType());
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should mark failed and send FAIL when listener throws")
    void testFailureCallsMarkFailedAndSendsFail() throws Exception {
        listener.throwOnEvent = true;
        EventMessage message = eventMessage("msg-004");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(1, handler.failedMarkCount());
        assertEquals(1, consumer.failedMessages.size());
        assertEquals(MessageType.FAIL, consumer.failedMessages.get(0).getMessageType());
        assertEquals("orderCreatedListener", consumer.failedMessages.get(0).getFailListener());
        assertFalse(handler.isMarked("msg-004", "orderCreatedListener"),
                "failed consumption must NOT be marked as processed");
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should dedup per listener independently")
    void testMultiListenerIndependent() throws Exception {
        OrderCreatedListener listener2 = new OrderCreatedListener();
        consumer.addListener("orderCreatedListener2", listener2);
        // Only listener1 is marked as processed.
        handler.seedProcessed("msg-005", "orderCreatedListener");

        EventMessage message = eventMessage("msg-005");
        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(0, listener.invocations(), "marked listener must be skipped");
        assertEquals(1, listener2.invocations(), "unmarked listener must execute");
        assertTrue(handler.isMarked("msg-005", "orderCreatedListener2"));
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should proceed (fail-open) when isProcessed throws")
    void testFailOpenOnCheckError() throws Exception {
        handler.throwOnCheck = true;
        EventMessage message = eventMessage("msg-006");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations(), "dedup failure must not block execution");
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should ack (fail-open) when markProcessed throws")
    void testFailOpenOnMarkProcessedError() throws Exception {
        handler.throwOnMarkProcessed = true;
        EventMessage message = eventMessage("msg-007");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations());
        assertTrue(consumer.failedMessages.isEmpty(), "marking failure must NOT be treated as consume failure");
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should continue failure flow (fail-open) when markFailed throws")
    void testFailOpenOnMarkFailedError() throws Exception {
        listener.throwOnEvent = true;
        handler.throwOnMarkFailed = true;
        EventMessage message = eventMessage("msg-008");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals(1, consumer.failedMessages.size(), "FAIL must still be sent");
        assertEquals(MessageType.FAIL, consumer.failedMessages.get(0).getMessageType());
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should honor listener-level override that disables dedup")
    void testListenerOverrideDisablesDedup() throws Exception {
        NoDedupListener noDedup = new NoDedupListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("noDedupListener", noDedup);
        TestableConsumer c = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, handler);
        // Even if the global handler marks it as processed, the override ignores it.
        handler.seedProcessed("msg-101", "noDedupListener");

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-101"));
        c.consumeRawMessage(rawPayload, ctx(), () -> {
        });
        c.consumeRawMessage(rawPayload, ctx(), () -> {
        });

        assertEquals(2, noDedup.invocations(), "dedup must be disabled for this listener");
        assertEquals(0, handler.markProcessedCalls(), "global handler must not be consulted");
    }

    @Test
    @DisplayName("Should use per-listener override logic instead of global handler")
    void testListenerOverrideCustomLogic() throws Exception {
        CustomKeyListener custom = new CustomKeyListener();
        Map<String, AbstractEventListener<?>> listeners = Map.of("customListener", custom);
        TestableConsumer c = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), listeners, handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-201"));
        c.consumeRawMessage(rawPayload, ctx(), () -> {
        });
        c.consumeRawMessage(rawPayload, ctx(), () -> {
        });

        assertEquals(1, custom.invocations(), "custom logic must dedup on its own key");
        assertEquals(0, handler.markProcessedCalls(), "global handler must not be consulted");
    }

    @Test
    @DisplayName("Should serialize concurrent duplicates of the same message (lock + mark)")
    void testLockSerializesConcurrentDuplicates() throws Exception {
        TestableConsumer c = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("orderCreatedListener", listener), handler);

        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        listener.enterLatch = entered;
        listener.releaseLatch = release;

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-301"));

        Thread a = new Thread(() -> c.consumeRawMessage(rawPayload, ctx(), () -> {
        }));
        Thread b = new Thread(() -> c.consumeRawMessage(rawPayload, ctx(), () -> {
        }));
        a.start();
        assertTrue(entered.await(2, TimeUnit.SECONDS), "thread A must enter doEvent");
        b.start();
        long deadline = System.currentTimeMillis() + 2000;
        while (handler.lockCalls() < 2 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(2, handler.lockCalls(), "thread B must have attempted the lock");
        assertEquals(0, listener.invocations(), "thread B must be blocked on the lock while A is inside doEvent");

        release.countDown();
        a.join();
        b.join();

        assertEquals(1, listener.invocations(), "duplicate must be skipped after the lock window");
        assertEquals(2, handler.unlockCalls(), "both acquisitions must be released");
    }

    @Test
    @DisplayName("Should proceed without lock (fail-open) when lock acquisition throws")
    void testLockFailureIsFailOpen() throws Exception {
        handler.throwOnLock = true;
        TestableConsumer c = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("orderCreatedListener", listener), handler);

        AtomicBoolean ack = new AtomicBoolean(false);
        c.consumeRawMessage(objectMapper.writeValueAsString(eventMessage("msg-302")), ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations(), "lock failure must not block execution");
        assertEquals(0, handler.unlockCalls(), "no unlock for a failed acquisition");
        assertTrue(c.failedMessages.isEmpty());
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should release the lock when listener throws")
    void testLockReleasedOnListenerException() throws Exception {
        listener.throwOnEvent = true;
        TestableConsumer c = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("orderCreatedListener", listener), handler);

        AtomicBoolean ack = new AtomicBoolean(false);
        c.consumeRawMessage(objectMapper.writeValueAsString(eventMessage("msg-303")), ctx(), () -> ack.set(true));

        assertEquals(1, handler.lockCalls());
        assertEquals(1, handler.unlockCalls(), "lock must be released on the failure path");
        assertEquals(1, c.failedMessages.size());
        assertEquals(MessageType.FAIL, c.failedMessages.get(0).getMessageType());
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Should skip without NPE when retry target listener is missing")
    void testRetryTargetMissingSkipsGracefully() throws Exception {
        EventMessage message = retryMessage("msg-999");
        // A listener that is not in the routing table (e.g. removed at runtime,
        // or registered on another module in a distributed deployment).
        message.setFailListener("notRegisteredListener");

        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertTrue(ack.get(), "message must be acked normally");
        assertEquals(0, listener.invocations());
        assertTrue(consumer.failedMessages.isEmpty(), "skip must not emit FAIL or RETRY_SUCCESS");
    }

    @Test
    @DisplayName("Should not be affected when a metrics collector throws (fail-open)")
    void testMetricsCollectorExceptionIsIsolated() throws Exception {
        // A collector that always throws must not pollute the consume result:
        // success must stay success, the message must still be acked, and no
        // FAIL must be emitted for a successfully consumed message.
        EventMetricsCollector throwingCollector = new EventMetricsCollector() {
            @Override
            public void onConsumeSuccess(String businessName, String listenerName) {
                throw new IllegalStateException("metrics failure");
            }

            @Override
            public void onConsumeFailure(String businessName, String listenerName, Throwable error) {
                throw new IllegalStateException("metrics failure");
            }

            @Override
            public void onConsumeLatency(String businessName, String listenerName, long durationMs, boolean success) {
                throw new IllegalStateException("metrics failure");
            }
        };
        TestableConsumer consumerWithThrowingMetrics = new TestableConsumer(
                objectMapper, eventProperties,
                List.of(throwingCollector),
                Map.of("orderCreatedListener", listener),
                handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-100"));
        AtomicBoolean ack = new AtomicBoolean(false);
        consumerWithThrowingMetrics.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true));

        assertEquals(1, listener.invocations(), "listener must execute normally");
        assertTrue(ack.get(), "message must be acked despite metrics failure");
        assertTrue(consumerWithThrowingMetrics.failedMessages.isEmpty(),
                "metrics failure must not turn success into FAIL");
    }

    @Test
    @DisplayName("Parallel mode should execute all listeners")
    void testParallelExecutesAllListeners() throws Exception {
        eventProperties.setConsumeParallel(true);
        OrderCreatedListener a = new OrderCreatedListener();
        OrderCreatedListener b = new OrderCreatedListener();
        consumer = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("listenerA", a, "listenerB", b), handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-p1"));
        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true));

        assertEquals(1, a.invocations(), "listener A must execute");
        assertEquals(1, b.invocations(), "listener B must execute");
        assertTrue(ack.get(), "message must be acked");
        assertTrue(consumer.failedMessages.isEmpty());
    }

    @Test
    @DisplayName("Parallel mode should not block fast listeners on a slow one")
    void testParallelSlowListenerDoesNotBlockFast() throws Exception {
        eventProperties.setConsumeParallel(true);
        OrderCreatedListener fast = new OrderCreatedListener();
        OrderCreatedListener slow = new OrderCreatedListener();
        slow.enterLatch = new CountDownLatch(1);
        slow.releaseLatch = new CountDownLatch(1);
        consumer = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("fast", fast, "slow", slow), handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-p2"));
        AtomicBoolean ack = new AtomicBoolean(false);
        Thread t = new Thread(() -> consumer.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true)));
        t.start();

        assertTrue(slow.enterLatch.await(2, TimeUnit.SECONDS), "slow listener must start");
        long deadline = System.currentTimeMillis() + 2000;
        while (fast.invocations() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(1, fast.invocations(), "fast listener must finish while slow listener is still blocked");

        slow.releaseLatch.countDown();
        t.join(3000);
        assertTrue(ack.get(), "message must be acked after all listeners complete");
        assertEquals(1, slow.invocations());
    }

    @Test
    @DisplayName("Parallel mode should propagate trace id into each listener thread")
    void testParallelPropagatesTraceId() throws Exception {
        eventProperties.setConsumeParallel(true);
        TraceAwareListener a = new TraceAwareListener();
        TraceAwareListener b = new TraceAwareListener();
        consumer = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("listenerA", a, "listenerB", b), handler);

        EventMessage message = eventMessage("msg-p3");
        message.setTraceId("trace-parallel");
        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(objectMapper.writeValueAsString(message), ctx(), () -> ack.set(true));

        assertEquals("trace-parallel", a.observedTraceId, "listener A must see the trace id");
        assertEquals("trace-parallel", b.observedTraceId, "listener B must see the trace id");
        assertTrue(ack.get());
    }

    @Test
    @DisplayName("Parallel mode should not ack when a listener aborts (send failure)")
    void testParallelSendFailurePreventsAck() throws Exception {
        eventProperties.setConsumeParallel(true);
        OrderCreatedListener failing = new OrderCreatedListener();
        failing.throwOnEvent = true;
        OrderCreatedListener ok = new OrderCreatedListener();
        consumer = new ThrowingSendConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("failing", failing, "ok", ok), handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-p4"));
        AtomicBoolean ack = new AtomicBoolean(false);
        assertThrows(RuntimeException.class,
                () -> consumer.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true)));
        assertFalse(ack.get(), "message must not be acked when a listener aborts");
    }

    @Test
    @DisplayName("Parallel mode should keep each FAIL signal on its own listener copy")
    void testParallelFailSignalsCarryOwnFailListener() throws Exception {
        eventProperties.setConsumeParallel(true);
        OrderCreatedListener a = new OrderCreatedListener();
        a.throwOnEvent = true;
        OrderCreatedListener b = new OrderCreatedListener();
        b.throwOnEvent = true;
        consumer = new TestableConsumer(objectMapper, eventProperties,
                Collections.emptyList(), Map.of("listenerA", a, "listenerB", b), handler);

        String rawPayload = objectMapper.writeValueAsString(eventMessage("msg-p5"));
        AtomicBoolean ack = new AtomicBoolean(false);
        consumer.consumeRawMessage(rawPayload, ctx(), () -> ack.set(true));

        assertEquals(2, consumer.failedMessages.size(), "two FAIL signals expected");
        Set<String> failListeners = new HashSet<>();
        for (EventMessage m : consumer.failedMessages) {
            failListeners.add(m.getFailListener());
        }
        assertEquals(Set.of("listenerA", "listenerB"), failListeners,
                "each FAIL must carry its own failListener");
        assertTrue(ack.get(), "business failures are handled inside, message still acked");
    }

    @Test
    @DisplayName("copy() should preserve all fields")
    void testMessageCopyPreservesAllFields() throws Exception {
        EventMessage message = eventMessage("msg-copy");
        message.setVersion(3);
        message.setTraceId("trace-copy");
        message.setErrorStack("stack");
        message.setFailListener("l1");
        message.setRetryCount(2);
        message.setRetryEnabled(true);
        message.setMessageType(MessageType.RETRY);
        message.setSource("src");
        message.setTopic("t");
        message.setData("{}");

        // @Data equals covers every field, so this fails if a future field is
        // added to EventMessage without being copied.
        assertEquals(message, message.copy(), "copy must preserve every field");
    }

    private EventMessage eventMessage(String id) throws Exception {
        EventMessage message = new EventMessage();
        message.setId(id);
        message.setSource("test-service");
        message.setBusinessName(listener.businessName());
        message.setTopic("test-topic");
        message.setData(objectMapper.writeValueAsString(new OrderCreatedEvent()));
        message.setMessageType(MessageType.EVENT);
        return message;
    }

    private EventMessage retryMessage(String id) throws Exception {
        EventMessage message = eventMessage(id);
        message.setMessageType(MessageType.RETRY);
        message.setFailListener("orderCreatedListener");
        return message;
    }

    private EventContext ctx() {
        return EventContext.builder()
                .timestamp(System.currentTimeMillis())
                .topic("test-topic")
                .build();
    }

    static class OrderCreatedEvent {
        private String orderId = "ORDER-001";

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }

    static class OrderCreatedListener extends AbstractEventListener<OrderCreatedEvent> {
        private final AtomicInteger invocations = new AtomicInteger();
        volatile boolean throwOnEvent;
        volatile CountDownLatch enterLatch;
        volatile CountDownLatch releaseLatch;

        OrderCreatedListener() {
            super(OrderCreatedEvent.class);
        }

        @Override
        public void doEvent(OrderCreatedEvent event, EventContext eventContext) {
            if (enterLatch != null) {
                enterLatch.countDown();
            }
            if (releaseLatch != null) {
                try {
                    releaseLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (throwOnEvent) {
                throw new IllegalStateException("simulated listener failure");
            }
            invocations.incrementAndGet();
        }

        int invocations() {
            return invocations.get();
        }
    }

    /**
     * Listener that records the trace id visible on its executing thread.
     */
    static class TraceAwareListener extends OrderCreatedListener {
        volatile String observedTraceId;

        @Override
        public void doEvent(OrderCreatedEvent event, EventContext eventContext) {
            observedTraceId = MDC.get("traceId");
            super.doEvent(event, eventContext);
        }
    }

    /**
     * Listener-level override that disables dedup entirely, ignoring the global handler.
     */
    static class NoDedupListener extends OrderCreatedListener {
        @Override
        public boolean isProcessed(EventMessage message) {
            return false;
        }

        @Override
        public void markProcessed(EventMessage message) {
            // no-op: this listener never participates in dedup
        }
    }

    /**
     * Listener-level override with its own dedup key (businessName + data),
     * completely replacing the global handler.
     */
    static class CustomKeyListener extends OrderCreatedListener {
        private final Set<String> processed = new HashSet<>();

        @Override
        public boolean isProcessed(EventMessage message) {
            return processed.contains(message.getBusinessName() + "::" + message.getData());
        }

        @Override
        public void markProcessed(EventMessage message) {
            processed.add(message.getBusinessName() + "::" + message.getData());
        }
    }

    /**
     * In-memory recording handler covering both idempotency marking and the
     * per-message lock, with switchable failure injection (fail-open tests).
     */
    static class RecordingIdempotencyHandler implements EventDedupHandler {
        private final Set<String> processed = new HashSet<>();
        private final Set<String> failedMarked = new HashSet<>();
        private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
        private final AtomicInteger lockCalls = new AtomicInteger();
        private final AtomicInteger unlockCalls = new AtomicInteger();
        boolean throwOnCheck;
        boolean throwOnMarkProcessed;
        boolean throwOnMarkFailed;
        boolean throwOnLock;
        private int markProcessedCalls;

        private static String key(EventMessage message, String listenerName) {
            return message.getId() + "::" + listenerName;
        }

        void seedProcessed(String messageId, String listenerName) {
            processed.add(messageId + "::" + listenerName);
        }

        boolean isMarked(String messageId, String listenerName) {
            return processed.contains(messageId + "::" + listenerName);
        }

        int markProcessedCalls() {
            return markProcessedCalls;
        }

        int markedProcessed() {
            return processed.size();
        }

        int failedMarkCount() {
            return failedMarked.size();
        }

        int lockCalls() {
            return lockCalls.get();
        }

        int unlockCalls() {
            return unlockCalls.get();
        }

        @Override
        public boolean isProcessed(EventMessage message, String listenerName) {
            if (throwOnCheck) {
                throw new IllegalStateException("simulated check failure");
            }
            return processed.contains(key(message, listenerName));
        }

        @Override
        public void markProcessed(EventMessage message, String listenerName) {
            markProcessedCalls++;
            if (throwOnMarkProcessed) {
                throw new IllegalStateException("simulated mark failure");
            }
            processed.add(key(message, listenerName));
        }

        @Override
        public void markFailed(EventMessage message, String listenerName, Throwable error) {
            if (throwOnMarkFailed) {
                throw new IllegalStateException("simulated markFailed failure");
            }
            failedMarked.add(key(message, listenerName));
        }

        @Override
        public void lock(String messageId) {
            if (throwOnLock) {
                throw new IllegalStateException("simulated lock failure");
            }
            lockCalls.incrementAndGet();
            locks.computeIfAbsent(messageId, k -> new ReentrantLock()).lock();
        }

        @Override
        public void unlock(String messageId) {
            unlockCalls.incrementAndGet();
            ReentrantLock lock = locks.get(messageId);
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Testable consumer that records every message sent to the failed topic,
     * so the RETRY_SUCCESS re-emission and FAIL paths can be asserted directly.
     */
    static class ThrowingSendConsumer extends TestableConsumer {
        ThrowingSendConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                             List<EventMetricsCollector> metricsCollectors,
                             Map<String, AbstractEventListener<?>> springListenerBeanMap,
                             EventDedupHandler dedupHandler) {
            super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, dedupHandler);
        }

        @Override
        protected void sendToFailedTopic(EventMessage message) {
            throw new IllegalStateException("failed topic unavailable");
        }
    }

    static class TestableConsumer extends EventMessageConsumer {
        private final List<EventMessage> failedMessages = new ArrayList<>();

        TestableConsumer(ObjectMapper jsonMapper, EventProperties eventProperties,
                         List<EventMetricsCollector> metricsCollectors,
                         Map<String, AbstractEventListener<?>> springListenerBeanMap,
                         EventDedupHandler dedupHandler) {
            super(jsonMapper, eventProperties, metricsCollectors, springListenerBeanMap, dedupHandler);
        }

        @Override
        protected void sendToFailedTopic(EventMessage message) {
            failedMessages.add(message);
        }

        @Override
        public void consumeRawMessage(String rawPayload, EventContext context, Runnable onSuccess) {
            super.consumeRawMessage(rawPayload, context, onSuccess);
        }
    }
}
