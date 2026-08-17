package io.github.springwhale.framework.event.server.kafka;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.server.EventConsumeFailedListener;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class KafkaEventConsumeFailedListener extends EventConsumeFailedListener {

    @KafkaListener(topics = "${spring.whale.event.failedTopic}", groupId = "spring-whale-event-server")
    public void listenerFailed(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            EventMessage message = jsonMapper.readValue(record.value(), EventMessage.class);
            if (message.getMessageType() != MessageType.FAIL) {
                ack.acknowledge();
                return;
            }
            handleMessage(message);
            ack.acknowledge();
        } catch (Exception e) {
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
            nextRetryTime = LocalDateTime.now().plusSeconds(eventProperties.getRetryInterval());
        }

        failedRecordRepository.updateRetryResult(
                message.getId(), status, nextRetryTime, message.getErrorStack());
    }

    private void createRetryRecord(EventMessage message) {
        EventConsumeFailedRecordEntity entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.PENDING_RETRY);
        entity.setRetryCount(1);
        entity.setNextRetryTime(LocalDateTime.now().plusSeconds(eventProperties.getRetryInterval()));
        failedRecordRepository.save(entity);
    }

    private void createDiscardRecord(EventMessage message) {
        EventConsumeFailedRecordEntity entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.DISCARDED);
        failedRecordRepository.save(entity);
    }
}