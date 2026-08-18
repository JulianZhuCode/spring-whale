package io.github.springwhale.framework.event.server.enums;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Status of a failed-event record in the retry lifecycle.
 */
@AllArgsConstructor
@Getter
public enum EventConsumeStatus implements BaseEnum {

    /** Awaiting the next retry cycle. */
    PENDING_RETRY("PENDING_RETRY", "Pending Retry"),
    /** Currently being retried (CAS-transitioned by a retry task instance). */
    RETRYING("RETRYING", "Retrying"),
    /** Terminal: failed after all retries exhausted. */
    FINAL_FAILED("FINAL_FAILED", "Final Failed"),
    /** Terminal: successfully replayed. */
    REPLAY_SUCCESS("REPLAY_SUCCESS", "Replay Success"),
    /** Terminal: discarded without retry (retryEnabled=false). */
    DISCARDED("DISCARDED", "Discarded");

    private final String id;
    private final String desc;
}