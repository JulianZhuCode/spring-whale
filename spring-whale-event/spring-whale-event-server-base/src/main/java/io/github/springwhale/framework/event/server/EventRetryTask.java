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
        eventMessage.setAuthenticationContext(jsonMapper.readValue(entity.getAuthenticationContext(), AuthenticationContext.class));
        eventMessage.setMessageType(MessageType.RETRY);
        eventMessage.setRetryCount(entity.getRetryCount() + 1);
        eventMessage.setRetryEnabled(true);
        eventMessage.setFailListener(entity.getListenerName());
        return eventMessage;
    }

    @Scheduled(fixedRateString = "${spring.whale.event.retryScheduleInterval:" + EventProperties.DEFAULT_RETRY_SCHEDULE_INTERVAL + "}")
    public void retry() {
        Page<EventConsumeFailedRecordEntity> page = recordRepository.findByStatusAndNextRetryTimeBefore(EventConsumeStatus.PENDING_RETRY, LocalDateTime.now(), PageRequest.of(0, eventProperties.getRetryBatchSize()));
        if (!page.hasContent()) {
            return;
        }
        List<EventConsumeFailedRecordEntity> entities = page.getContent();
        log.info("Found [{}] failed messages to retry", entities.size());
        for (EventConsumeFailedRecordEntity entity : entities) {
            EventMessage eventMessage = buildEventMessage(entity);
            eventPublisher.publish(eventMessage);
            recordRepository.updateRetryStatus(entity.getMessageId(), EventConsumeStatus.RETRYING);
        }
    }

}