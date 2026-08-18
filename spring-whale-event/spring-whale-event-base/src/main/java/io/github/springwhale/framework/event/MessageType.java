package io.github.springwhale.framework.event;

/**
 * Type of event message flowing through the system.
 */
public enum MessageType {
    /** A normal business event published by the application. */
    EVENT,
    /** A retry message re-published by the retry task. */
    RETRY,
    /** A failed-event message sent to the failed topic for retry processing. */
    FAIL
}