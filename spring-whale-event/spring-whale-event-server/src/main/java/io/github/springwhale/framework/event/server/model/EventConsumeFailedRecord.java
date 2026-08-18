package io.github.springwhale.framework.event.server.model;

import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventConsumeFailedRecord {

    private String id;

    private String messageId;

    private String source;

    private String businessName;

    private String listenerName;

    private String authenticationContext;

    private String topic;

    private String rawMessage;

    private EventConsumeStatus status = EventConsumeStatus.PENDING_RETRY;

    private int retryCount;

    private LocalDateTime nextRetryTime;

    private String errorStack;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}