package io.github.springwhale.framework.event.recovery.model;

import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import lombok.Data;

import java.util.List;

/**
 * Request parameters for manually resetting records to pending retry.
 * <p>At least one of {@code ids} or {@code status} must be provided.
 * {@code status} only accepts terminal states ({@code FINAL_FAILED}, {@code DISCARDED}, {@code REPLAY_SUCCESS}).</p>
 */
@Data
public class ResetRetryRequest {

    private List<String> ids;

    private EventConsumeStatus status;

    private boolean resetRetryCount = true;

}