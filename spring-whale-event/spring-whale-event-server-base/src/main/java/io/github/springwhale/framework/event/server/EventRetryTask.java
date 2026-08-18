package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRetryTask {
    private final EventConsumeFailedRecordRepository recordRepository;
    private final EventProperties eventProperties;
    private final EventPublisher eventPublisher;
    private final ObjectMapper jsonMapper;

    private @NonNull EventMessage buildEventMessage(EventConsumeFailedRecordEntity entity) {
        EventMessage eventMessage = new EventMessage();
        eventMessage.setId(entity.getMessageId());
        eventMessage.setSource(entity.getSource());
        eventMessage.setData(entity.getRawMessage());
        eventMessage.setBusinessName(entity.getBusinessName());
        eventMessage.setTopic(entity.getTopic());
        try {
            eventMessage.setAuthenticationContext(jsonMapper.readValue(entity.getAuthenticationContext(), AuthenticationContext.class));
        } catch (Exception e) {
            log.warn("parse authentication context failed", e);
        }
        eventMessage.setMessageType(MessageType.RETRY);
        eventMessage.setRetryCount(entity.getRetryCount() + 1);
        eventMessage.setRetryEnabled(true);
        eventMessage.setFailListener(entity.getListenerName());
        return eventMessage;
    }

    /**
     * Scheduled retry task for failed event messages.
     * <p>Uses "publish-first, then update status" strategy: the message is sent to Kafka
     * synchronously first, then the database status is updated via CAS. If the Kafka send
     * succeeds but the DB update fails, the message will be picked up again on the next
     * retry cycle (duplicate delivery is acceptable — at-least-once semantics).
     * Message loss is NOT acceptable, so if the Kafka send fails, the status is not
     * updated and the message will be retried on the next schedule.</p>
     * <p>Each record is processed in its own try-catch block so that one record's
     * failure does not interrupt the entire batch.</p>
     */
    @Scheduled(fixedDelayString = "${spring.whale.event.retryScheduleInterval:" + EventProperties.DEFAULT_RETRY_SCHEDULE_INTERVAL + "}")
    public void retry() {
        Page<EventConsumeFailedRecordEntity> page = recordRepository.findByStatusAndNextRetryTimeBefore(EventConsumeStatus.PENDING_RETRY, LocalDateTime.now(),
                PageRequest.of(0, eventProperties.getRetryBatchSize(), Sort.by(Sort.Direction.ASC, "nextRetryTime")));
        if (!page.hasContent()) {
            return;
        }
        List<EventConsumeFailedRecordEntity> entities = page.getContent();
        log.info("Found [{}] failed messages to retry", entities.size());
        for (EventConsumeFailedRecordEntity entity : entities) {
            try {
                EventMessage eventMessage = buildEventMessage(entity);
                eventPublisher.publish(eventMessage);
                recordRepository.casTransitionStatus(entity.getId(), EventConsumeStatus.PENDING_RETRY, EventConsumeStatus.RETRYING);
            } catch (Exception e) {
                // Publish failed: do NOT update status, retry on next schedule.
                // Do NOT rethrow — one record's failure should not interrupt the batch.
                log.error("Retry publish or update status failed for messageId={}", entity.getMessageId(), e);
            }
        }
    }

}