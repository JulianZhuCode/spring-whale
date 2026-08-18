package io.github.springwhale.framework.event.server.util;

import java.util.UUID;

/**
 * Generates a deterministic record ID for failed-event records.
 * <p>
 * The ID is derived from the original event message ID and the listener name,
 * ensuring that a given (messageId, listenerName) pair always produces the
 * same ID. This makes the record queryable by business callers without
 * needing to know the internal ID.
 * </p>
 */
public final class EventFailedRecordIdGenerator {

    private EventFailedRecordIdGenerator() {
    }

    /**
     * Generates a deterministic UUID from the message ID and listener name.
     *
     * @param messageId    the original event message ID
     * @param listenerName the name of the listener that failed
     * @return a deterministic UUID string
     */
    public static String generate(String messageId, String listenerName) {
        return UUID.nameUUIDFromBytes((messageId + listenerName).getBytes()).toString();
    }
}