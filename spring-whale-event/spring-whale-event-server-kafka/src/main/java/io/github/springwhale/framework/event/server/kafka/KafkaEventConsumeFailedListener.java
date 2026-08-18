package io.github.springwhale.framework.event.server.kafka;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.server.EventConsumeFailedListener;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.server.util.EventFailedRecordIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class KafkaEventConsumeFailedListener extends EventConsumeFailedListener {

    /**
     * Listener for the failed-event topic. Single consumer group is intentional:
     * horizontal scaling is not needed for processing failed-event records,
     * which are low-volume by nature (only produced on listener exceptions).
     * <p>If processing fails for any reason (e.g. database unavailable), the catch
     * block intentionally does NOT acknowledge the message — Kafka will re-deliver
     * it once the system recovers. This provides at-least-once semantics.</p>
     */
    @KafkaListener(topics = "#{@eventProperties.failedTopic}",
            concurrency = "#{@kafkaEventProperties.failedConcurrency}",
            groupId = "#{@kafkaEventProperties.failedGroupId}")
    public void listenerFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            EventMessage message = jsonMapper.readValue(record.value(), EventMessage.class);
            if (message.getMessageType() != MessageType.FAIL) {
                log.debug("Received non-fail message: {}", message);
                ack.acknowledge();
                return;
            }
            handleMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
            // Intentionally do NOT acknowledge: at-least-once semantics.
            // If processing fails (e.g. database unavailable), Kafka will
            // re-deliver the message once the system recovers. No data loss.
            log.error("Failed to process event message: {}", record.value(), e);
        }
    }

    private void handleMessage(EventMessage message) {
        int retryCount = Objects.requireNonNullElse(message.getRetryCount(), 0);

        if (retryCount > 0) {
            updateRetryRecord(message, retryCount);
        } else if (Boolean.TRUE.equals(message.getRetryEnabled())) {
            createRetryRecord(message);
        } else {
            createDiscardRecord(message);
        }
    }

    private void updateRetryRecord(EventMessage message, int retryCount) {
        EventConsumeStatus status;
        LocalDateTime nextRetryTime;

        if (Boolean.TRUE.equals(message.getRetrySuccess())) {
            status = EventConsumeStatus.REPLAY_SUCCESS;
            nextRetryTime = null;
        } else if (retryCount >= eventProperties.getMaxRetries()) {
            status = EventConsumeStatus.DISCARDED;
            nextRetryTime = null;
        } else {
            status = EventConsumeStatus.PENDING_RETRY;
            nextRetryTime = computeNextRetryTime(retryCount);
        }

        failedRecordRepository.updateRetryResult(
                EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()),
                status, nextRetryTime, message.getErrorStack());
    }

    private void createRetryRecord(EventMessage message) {
        EventConsumeFailedRecordEntity entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.PENDING_RETRY);
        entity.setRetryCount(1);
        entity.setNextRetryTime(computeNextRetryTime(1));
        failedRecordRepository.save(entity);
    }

    private void createDiscardRecord(EventMessage message) {
        EventConsumeFailedRecordEntity entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.DISCARDED);
        failedRecordRepository.save(entity);
    }

    private @NonNull LocalDateTime computeNextRetryTime(int retryCount) {
        return LocalDateTime.now().plusSeconds(
                eventProperties.getRetryStrategy().calculateDelay(
                        eventProperties.getRetryInterval(),
                        eventProperties.getRetryMaxInterval(),
                        retryCount));
    }
}