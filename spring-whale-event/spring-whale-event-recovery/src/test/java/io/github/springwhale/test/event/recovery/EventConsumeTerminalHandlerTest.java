package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class EventConsumeTerminalHandlerTest {

    static class TestTerminalHandler implements EventConsumeTerminalHandler {
        private final AtomicBoolean discardedCalled = new AtomicBoolean(false);
        private final AtomicBoolean finalFailedCalled = new AtomicBoolean(false);

        @Override
        public void onDiscarded(EventConsumeFailedRecord record) {
            discardedCalled.set(true);
        }

        @Override
        public void onFinalFailed(EventConsumeFailedRecord record) {
            finalFailedCalled.set(true);
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    @Test
    @DisplayName("Should invoke onDiscarded")
    void testOnDiscarded() {
        TestTerminalHandler handler = new TestTerminalHandler();
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setMessageId("msg-001");

        handler.onDiscarded(record);
        assertTrue(handler.discardedCalled.get());
    }

    @Test
    @DisplayName("Should invoke onFinalFailed")
    void testOnFinalFailed() {
        TestTerminalHandler handler = new TestTerminalHandler();
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setMessageId("msg-001");

        handler.onFinalFailed(record);
        assertTrue(handler.finalFailedCalled.get());
    }

    @Test
    @DisplayName("Default getOrder should return 0")
    void testDefaultOrder() {
        EventConsumeTerminalHandler handler = new EventConsumeTerminalHandler() {
            @Override
            public void onDiscarded(EventConsumeFailedRecord record) {
            }

            @Override
            public void onFinalFailed(EventConsumeFailedRecord record) {
            }
        };

        assertEquals(0, handler.getOrder());
    }

    @Test
    @DisplayName("Custom order should be respected")
    void testCustomOrder() {
        EventConsumeTerminalHandler handler = new EventConsumeTerminalHandler() {
            @Override
            public void onDiscarded(EventConsumeFailedRecord record) {
            }

            @Override
            public void onFinalFailed(EventConsumeFailedRecord record) {
            }

            @Override
            public int getOrder() {
                return 100;
            }
        };

        assertEquals(100, handler.getOrder());
    }

    @Test
    @DisplayName("Multiple handlers should be ordered by getOrder")
    void testHandlerOrdering() {
        EventConsumeTerminalHandler low = new EventConsumeTerminalHandler() {
            @Override
            public void onDiscarded(EventConsumeFailedRecord r) {}

            @Override
            public void onFinalFailed(EventConsumeFailedRecord r) {}

            @Override
            public int getOrder() { return 10; }
        };

        EventConsumeTerminalHandler high = new EventConsumeTerminalHandler() {
            @Override
            public void onDiscarded(EventConsumeFailedRecord r) {}

            @Override
            public void onFinalFailed(EventConsumeFailedRecord r) {}

            @Override
            public int getOrder() { return 20; }
        };

        List<EventConsumeTerminalHandler> handlers = new ArrayList<>();
        handlers.add(high);
        handlers.add(low);

        handlers.sort(java.util.Comparator.comparingInt(EventConsumeTerminalHandler::getOrder));

        assertEquals(10, handlers.get(0).getOrder());
        assertEquals(20, handlers.get(1).getOrder());
    }
}