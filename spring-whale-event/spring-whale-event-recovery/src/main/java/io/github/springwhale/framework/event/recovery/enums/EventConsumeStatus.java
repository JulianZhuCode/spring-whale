package io.github.springwhale.framework.event.recovery.enums;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status of a failed-event record in the retry lifecycle.
 */
@AllArgsConstructor
@Getter
public enum EventConsumeStatus implements BaseEnum {

    /**
     * Awaiting the next retry cycle.
     */
    PENDING_RETRY("event.consume.status.pending_retry", "Pending Retry"),
    /**
     * Currently being retried (CAS-transitioned by a retry task instance).
     */
    RETRYING("event.consume.status.retrying", "Retrying"),
    /**
     * Terminal: failed after all retries exhausted.
     */
    FINAL_FAILED("event.consume.status.final_failed", "Final Failed"),
    /**
     * Terminal: successfully replayed.
     */
    REPLAY_SUCCESS("event.consume.status.replay_success", "Replay Success"),
    /**
     * Terminal: discarded without retry (retryEnabled=false).
     */
    DISCARDED("event.consume.status.discarded", "Discarded");

    private final String id;
    private final String desc;
}