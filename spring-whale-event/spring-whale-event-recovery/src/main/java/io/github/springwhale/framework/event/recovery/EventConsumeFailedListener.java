package io.github.springwhale.framework.event.recovery;

import io.github.springwhale.framework.event.*;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import io.github.springwhale.framework.event.recovery.util.EventFailedRecordIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class EventConsumeFailedListener {
    protected final EventConsumeFailedRecordDao failedRecordDao;
    protected final EventProperties eventProperties;
    protected final ObjectMapper jsonMapper;
    protected final RetryStrategyRegistry retryStrategyRegistry;
    protected final List<EventMetricsCollector> metricsCollectors;
    protected final List<EventConsumeTerminalHandler> terminalHandlers;

    public EventConsumeFailedListener(EventConsumeFailedRecordDao failedRecordDao,
                                      EventProperties eventProperties, ObjectMapper jsonMapper,
                                      RetryStrategyRegistry retryStrategyRegistry,
                                      List<EventMetricsCollector> metricsCollectors,
                                      List<EventConsumeTerminalHandler> terminalHandlers) {
        this.failedRecordDao = failedRecordDao;
        this.eventProperties = eventProperties;
        this.jsonMapper = jsonMapper;
        this.retryStrategyRegistry = retryStrategyRegistry;
        this.metricsCollectors = metricsCollectors != null ? metricsCollectors : Collections.emptyList();
        this.terminalHandlers = terminalHandlers != null ? terminalHandlers : Collections.emptyList();
    }

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
            status = EventConsumeStatus.FINAL_FAILED;
            nextRetryTime = null;
            metricsCollectors.forEach(c -> c.onRetryExhausted(message.getId(), message.getFailListener(), retryCount));
        } else {
            status = EventConsumeStatus.PENDING_RETRY;
            nextRetryTime = computeNextRetryTime(retryCount);
        }

        failedRecordDao.updateRetryResult(
                EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()),
                status, nextRetryTime, message.getErrorStack());

        if (status == EventConsumeStatus.FINAL_FAILED) {
            failedRecordDao
                    .findById(EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener())).ifPresent(record -> terminalHandlers.stream()
                            .sorted(Comparator.comparingInt(EventConsumeTerminalHandler::getOrder))
                            .forEach(h -> h.onFinalFailed(record)));
        }
    }

    private void createRetryRecord(EventMessage message) {
        EventConsumeFailedRecord entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.PENDING_RETRY);
        entity.setRetryCount(1);
        entity.setNextRetryTime(computeNextRetryTime(1));
        failedRecordDao.save(entity);
    }

    private void createDiscardRecord(EventMessage message) {
        EventConsumeFailedRecord entity = buildRecordEntity(message);
        entity.setStatus(EventConsumeStatus.DISCARDED);
        failedRecordDao.save(entity);
        metricsCollectors.forEach(c -> c.onRetryExhausted(message.getId(), message.getFailListener(), 0));
        terminalHandlers.stream()
                .sorted(Comparator.comparingInt(EventConsumeTerminalHandler::getOrder))
                .forEach(h -> h.onDiscarded(entity));
    }

    private @NonNull LocalDateTime computeNextRetryTime(int retryCount) {
        RetryStrategy strategy = retryStrategyRegistry.get(eventProperties.getRetryStrategy());
        return LocalDateTime.now().plusSeconds(
                strategy.calculateDelay(
                        eventProperties.getRetryIntervalSeconds(),
                        eventProperties.getRetryMaxInterval(),
                        retryCount));
    }

    protected @NonNull EventConsumeFailedRecord buildRecordEntity(EventMessage message) {
        EventConsumeFailedRecord entity = new EventConsumeFailedRecord();
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