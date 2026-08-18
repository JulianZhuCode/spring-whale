package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.RetryStrategy;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import io.github.springwhale.framework.event.server.util.EventFailedRecordIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class EventConsumeFailedListener {
    @Autowired
    protected EventConsumeFailedRecordRepository failedRecordRepository;
    @Autowired
    protected EventProperties eventProperties;
    @Autowired
    protected ObjectMapper jsonMapper;
    @Autowired
    protected RetryStrategyRegistry retryStrategyRegistry;
    @Autowired(required = false)
    protected List<EventMetricsCollector> metricsCollectors = Collections.emptyList();
    @Autowired(required = false)
    protected List<EventConsumeTerminalHandler> terminalHandlers = Collections.emptyList();

    /**
     * Handle a failed-event message: determine status and persist the record.
     * <p>Called by MQ-specific subclasses after deserializing the raw message.</p>
     */
    protected void handleMessage(EventMessage message) {
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
            metricsCollectors.forEach(c -> c.onRetrySuccess(message.getId(), message.getFailListener()));
        } else if (retryCount >= eventProperties.getMaxRetries()) {
            status = EventConsumeStatus.DISCARDED;
            nextRetryTime = null;
            metricsCollectors.forEach(c -> c.onRetryExhausted(message.getId(), message.getFailListener(), retryCount));
        } else {
            status = EventConsumeStatus.PENDING_RETRY;
            nextRetryTime = computeNextRetryTime(retryCount);
        }

        failedRecordRepository.updateRetryResult(
                EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()),
                status, nextRetryTime, message.getErrorStack());

        if (status == EventConsumeStatus.DISCARDED) {
            EventConsumeFailedRecordEntity record = failedRecordRepository
                    .findById(EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()))
                    .orElse(null);
            if (record != null) {
                terminalHandlers.stream()
                        .sorted(Comparator.comparingInt(EventConsumeTerminalHandler::getOrder))
                        .forEach(h -> h.onDiscarded(record));
            }
        }
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
        metricsCollectors.forEach(c -> c.onRetryExhausted(message.getId(), message.getFailListener(), 0));
        terminalHandlers.stream()
                .sorted(Comparator.comparingInt(EventConsumeTerminalHandler::getOrder))
                .forEach(h -> h.onDiscarded(entity));
    }

    private @NonNull LocalDateTime computeNextRetryTime(int retryCount) {
        RetryStrategy strategy = retryStrategyRegistry.get(eventProperties.getRetryStrategy());
        return LocalDateTime.now().plusSeconds(
                strategy.calculateDelay(
                        eventProperties.getRetryInterval(),
                        eventProperties.getRetryMaxInterval(),
                        retryCount));
    }

    protected @NonNull EventConsumeFailedRecordEntity buildRecordEntity(EventMessage message) {
        EventConsumeFailedRecordEntity entity = new EventConsumeFailedRecordEntity();
        entity.setId(EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()));
        entity.setMessageId(message.getId());
        entity.setSource(message.getSource());
        entity.setBusinessName(message.getBusinessName());
        entity.setListenerName(message.getFailListener());
        entity.setAuthenticationContext(jsonMapper.writeValueAsString(message.getAuthenticationContext()));
        entity.setTopic(message.getTopic());
        entity.setRawMessage(message.getData());
        entity.setErrorStack(message.getErrorStack());
        return entity;
    }
}