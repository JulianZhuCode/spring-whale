package io.github.springwhale.framework.event;

/**
 * SPI for deduplicating event message consumption.
 * <p>A single handler is responsible for the two mechanisms that together prevent
 * a message from being processed more than once:</p>
 * <ul>
 *   <li><b>Idempotency marking</b> ({@link #isProcessed}/{@link #markProcessed}/{@link #markFailed}):
 *       keyed by (message id, listener name), prevents duplicates delivered after the
 *       first processing completed.</li>
 *   <li><b>Per-message lock</b> ({@link #lock}/{@link #unlock}): keyed by message id,
 *       serializes concurrent deliveries so the check-execute-mark window of two
 *       overlapping duplicates is closed.</li>
 * </ul>
 * <p>All methods are part of the contract and must be implemented. Implement methods
 * you do not need as no-ops (e.g. {@code markFailed} for success-marking implementations,
 * {@code lock}/{@code unlock} for deployments relying on serial delivery without
 * concurrent duplicates).</p>
 * <p>The handler is optional: when no bean is registered, the consumer behaves exactly
 * as before (no dedup, no locking). Register one bean and it is used both by the
 * {@link AbstractEventListener} hooks and by the consumer's dispatch path.</p>
 * <p>Contract notes:</p>
 * <ul>
 *   <li>{@code lock} must block until the lock for the message id is acquired, and
 *       {@code unlock} is called exactly once after a successful {@code lock}
 *       (the consumer guarantees this, including on failure paths).</li>
 *   <li>A lock acquisition failure is fail-open: the consumer logs the error and
 *       continues without the lock for that message.</li>
 *   <li>Storage failures in the marking methods are also fail-open: the listener's
 *       default implementations log and continue.</li>
 * </ul>
 */
public interface EventDedupHandler {

    /**
     * Whether this message has already been successfully processed by this listener.
     *
     * @param message      the event message
     * @param listenerName the listener's registered name (part of the dedup key)
     * @return true if the message is a duplicate and should be skipped
     */
    boolean isProcessed(EventMessage message, String listenerName);

    /**
     * Mark the message as successfully processed by this listener.
     * <p>Must be idempotent (repeated marking is harmless).</p>
     *
     * @param message      the event message
     * @param listenerName the listener's registered name (part of the dedup key)
     */
    void markProcessed(EventMessage message, String listenerName);

    /**
     * Mark the message as failed for this listener.
     * <p>Claim-based implementations use it to release the claim so a later retry
     * is not treated as a duplicate. Success-marking implementations may no-op.</p>
     *
     * @param message      the event message
     * @param listenerName the listener's registered name
     * @param error        the exception thrown by the listener
     */
    void markFailed(EventMessage message, String listenerName, Throwable error);

    /**
     * Acquire the lock for the given message id, blocking until acquired.
     *
     * @param messageId the event message id (never {@code null} when called by the framework)
     * @throws RuntimeException if the lock cannot be acquired
     *                          (e.g. bounded wait timeout, backend unavailable)
     */
    void lock(String messageId);

    /**
     * Release the lock for the given message id.
     *
     * @param messageId the event message id
     */
    void unlock(String messageId);
}
