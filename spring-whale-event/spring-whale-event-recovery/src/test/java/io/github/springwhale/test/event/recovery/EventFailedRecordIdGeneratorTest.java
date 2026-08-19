package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.util.EventFailedRecordIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventFailedRecordIdGeneratorTest {

    @Test
    @DisplayName("Same messageId and listenerName should produce identical ID")
    void testDeterministic() {
        String id1 = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");
        String id2 = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");

        assertEquals(id1, id2);
    }

    @Test
    @DisplayName("Different messageId should produce different IDs")
    void testDifferentMessageId() {
        String id1 = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");
        String id2 = EventFailedRecordIdGenerator.generate("msg-002", "orderListener");

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Different listenerName should produce different IDs")
    void testDifferentListenerName() {
        String id1 = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");
        String id2 = EventFailedRecordIdGenerator.generate("msg-001", "paymentListener");

        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("Should produce valid UUID format")
    void testValidUUIDFormat() {
        String id = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");

        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertEquals(36, id.length());
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        String id = EventFailedRecordIdGenerator.generate("", "");

        assertNotNull(id);
        assertFalse(id.isEmpty());
    }
}