package io.github.springwhale.framework.event.recovery;

import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;

/**
 * SPI interface for handling messages that have reached a terminal state.
 * <p>Implementations are discovered via {@code @Autowired(required = false) List<EventConsumeTerminalHandler>}
 * and invoked in order of {@link #getOrder()} (ascending).</p>
 * <p>Typical use cases: send alerts (DingTalk/email/webhook), write to a separate
 * monitoring table, trigger compensating transactions.</p>
 */
public interface EventConsumeTerminalHandler {

    /**
     * Called when a message is discarded (retries exhausted or {@code retryEnabled=false}).
     *
     * @param record the failed record entity with full context
     */
    void onDiscarded(EventConsumeFailedRecord record);

    /**
     * Called when a message has reached a terminal failure state (all retries exhausted).
     *
     * @param record the failed record entity with full context
     */
    void onFinalFailed(EventConsumeFailedRecord record);

    /**
     * Execution order. Lower values execute first. Default is 0.
     */
    default int getOrder() {
        return 0;
    }

}