package io.github.springwhale.framework.event.server.enums;

import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventConsumeStatus implements BaseEnum {

    PENDING_RETRY("PENDING_RETRY", "Pending Retry"),
    FINAL_FAILED("FINAL_FAILED", "Final Failed"),
    REPLAY_SUCCESS("REPLAY_SUCCESS", "Replay Success"),
    DISCARDED("DISCARDED", "Discarded");

    private final String id;
    private final String desc;
}