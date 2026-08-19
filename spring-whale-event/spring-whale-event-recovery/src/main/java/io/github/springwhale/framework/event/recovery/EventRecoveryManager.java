package io.github.springwhale.framework.event.recovery;

import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.ResetRetryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Manual recovery manager for failed event records.
 * <p>Provides the ability to manually reset records to {@code PENDING_RETRY} status,
 * so they will be picked up by {@link EventRetryTask} on the next scheduled cycle.</p>
 * <p>Usage: inject {@code EventRecoveryManager} and call {@link #resetRetry(ResetRetryRequest)}
 * with the desired filter criteria.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRecoveryManager {

    private static final Set<EventConsumeStatus> TERMINAL_STATUSES = EnumSet.of(
            EventConsumeStatus.FINAL_FAILED,
            EventConsumeStatus.DISCARDED,
            EventConsumeStatus.REPLAY_SUCCESS
    );

    private final EventConsumeFailedRecordDao recordDao;

    /**
     * Reset matching records to {@code PENDING_RETRY} so they will be retried by
     * the scheduled retry task.
     * <p>At least one of {@code ids} or {@code status} must be provided in the request.
     * The {@code status} filter only accepts terminal states
     * ({@code FINAL_FAILED}, {@code DISCARDED}, {@code REPLAY_SUCCESS}).</p>
     *
     * @param request the filter criteria and reset options
     * @return the number of records affected
     * @throws IllegalArgumentException if no filter criteria is provided or status is not terminal
     */
    public int resetRetry(ResetRetryRequest request) {
        validate(request);

        List<String> ids = request.getIds();
        List<EventConsumeStatus> statuses = request.getStatus() != null
                ? List.of(request.getStatus()) : Collections.emptyList();

        int affected = recordDao.batchResetToPendingRetry(ids, statuses, request.isResetRetryCount());
        log.info("Reset {} records to PENDING_RETRY. ids={}, status={}, resetRetryCount={}",
                affected, ids, request.getStatus(), request.isResetRetryCount());
        return affected;
    }

    private void validate(ResetRetryRequest request) {
        boolean hasIds = request.getIds() != null && !request.getIds().isEmpty();
        boolean hasStatus = request.getStatus() != null;

        if (!hasIds && !hasStatus) {
            throw new IllegalArgumentException("At least one of ids or status must be provided");
        }

        if (hasStatus && !TERMINAL_STATUSES.contains(request.getStatus())) {
            throw new IllegalArgumentException(
                    "Status must be a terminal state: " + TERMINAL_STATUSES + ", but was: " + request.getStatus());
        }
    }

}