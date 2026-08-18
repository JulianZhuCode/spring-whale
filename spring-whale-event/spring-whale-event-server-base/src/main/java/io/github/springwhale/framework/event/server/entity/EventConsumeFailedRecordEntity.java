package io.github.springwhale.framework.event.server.entity;

import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_consume_failed_record", indexes = {
        @Index(name = "idx_event_consume_failed_record_next_retry_time", columnList = "nextRetryTime")
})
@Data
public class EventConsumeFailedRecordEntity {

    @Id
    @Column(nullable = false, length = 64)
    private String messageId;

    @Column(length = 128)
    private String source;

    @Column(length = 128)
    private String businessName;

    @Column(length = 128)
    private String listenerName;

    @Column(columnDefinition = "text")
    private String authenticationContext;

    @Column(length = 256)
    private String topic;

    @Column(columnDefinition = "text")
    private String rawMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EventConsumeStatus status = EventConsumeStatus.PENDING_RETRY;

    private Integer retryCount = 0;

    private LocalDateTime nextRetryTime;

    @Column(columnDefinition = "text")
    private String errorStack;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}