package io.github.springwhale.framework.event;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when one or more events in a batch publish fail.
 * <p>Contains the full list of failures, each with the event index,
 * the event object, and the cause exception.</p>
 */
public class BatchPublishException extends Exception {

    private final List<Failure> failures;

    public BatchPublishException(List<Failure> failures) {
        super("Batch publish failed: " + failures.size() + " of " + failures.size() + " events failed");
        this.failures = Collections.unmodifiableList(failures);
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public static class Failure {
        private final int index;
        private final Object event;
        private final Throwable cause;

        public Failure(int index, Object event, Throwable cause) {
            this.index = index;
            this.event = event;
            this.cause = cause;
        }

        public int getIndex() {
            return index;
        }

        public Object getEvent() {
            return event;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}