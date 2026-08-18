package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class EventRetryTask {
    private final EventConsumeFailedRecordRepository recordRepository;
    private final EventProperties eventProperties;
    private final EventPublisher eventPublisher;
    private final ObjectMapper jsonMapper;
    private final List<EventMetricsCollector> metricsCollectors;
    private final ExecutorService retryExecutor;

    public EventRetryTask(EventConsumeFailedRecordRepository recordRepository,
                          EventProperties eventProperties,
                          EventPublisher eventPublisher,
                          ObjectMapper jsonMapper,
                          List<EventMetricsCollector> metricsCollectors) {
        this.recordRepository = recordRepository;
        this.eventProperties = eventProperties;
        this.eventPublisher = eventPublisher;
        this.jsonMapper = jsonMapper;
        this.metricsCollectors = metricsCollectors != null ? metricsCollectors : Collections.emptyList();
        this.retryExecutor = new ThreadPoolExecutor(
                eventProperties.getRetryThreadPoolSize(),
                eventProperties.getRetryThreadPoolSize(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Thread.ofVirtual().name("event-retry-", 0).factory()
        );
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down retry executor...");
        retryExecutor.shutdown();
        try {
            if (!retryExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            retryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

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
     * <p>Uses "publish-first, then update status" strategy: the message is published
     * to the MQ broker synchronously, then the database status is updated via CAS.
     * If the publish succeeds but the DB update fails, the record will be picked up
     * again on the next retry cycle (duplicate delivery is acceptable for at-least-once
     * semantics). If the publish fails, the status is not updated and the message
     * will be retried on the next schedule.</p>
     * <p>Multi-instance safety: CAS on the status field prevents duplicate DB updates.
     * When two instances race on the same record, both will publish (duplicate message),
     * but only one succeeds in the CAS transition from PENDING_RETRY to RETRYING.
     * The other sees 0 affected rows and the record is safely skipped from DB update.</p>
     * <p>Each record is handled in its own try-catch, so one record's failure does not
     * interrupt others. A thread pool is used for parallel processing.</p>
     */
    @Scheduled(fixedDelayString = "${spring.whale.event.retryScheduleInterval:" + EventProperties.DEFAULT_RETRY_SCHEDULE_INTERVAL + "}")
    public void retry() {
        Page<EventConsumeFailedRecordEntity> page = recordRepository.findByStatusAndNextRetryTimeBefore(
                EventConsumeStatus.PENDING_RETRY, LocalDateTime.now(),
                PageRequest.of(0, eventProperties.getRetryBatchSize(), Sort.by(Sort.Direction.ASC, "nextRetryTime")));
        if (!page.hasContent()) {
            return;
        }
        List<EventConsumeFailedRecordEntity> entities = page.getContent();
        log.info("Found [{}] failed messages to retry", entities.size());

        CompletableFuture<?>[] futures = entities.stream()
                .map(entity -> CompletableFuture.runAsync(() -> retrySingle(entity), retryExecutor))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(futures).get(
                    (long) eventProperties.getSendTimeoutSeconds() * entities.size(),
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Retry batch timed out or was interrupted after {}s. "
                            + "{} records will be retried on next schedule.",
                    eventProperties.getSendTimeoutSeconds() * entities.size(), entities.size(), e);
        }
    }

    private void retrySingle(EventConsumeFailedRecordEntity entity) {
        try {
            EventMessage eventMessage = buildEventMessage(entity);
            eventPublisher.publish(eventMessage);
            recordRepository.casTransitionStatus(entity.getId(), EventConsumeStatus.PENDING_RETRY, EventConsumeStatus.RETRYING);
            metricsCollectors.forEach(c -> c.onRetryScheduled(entity.getMessageId(), entity.getListenerName(), eventMessage.getRetryCount()));
        } catch (Exception e) {
            log.error("Retry publish or update status failed for messageId={}", entity.getMessageId(), e);
        }
    }

    /**
     * Scheduled cleanup of terminal records (DISCARDED, REPLAY_SUCCESS, FINAL_FAILED)
     * older than {@code retryCleanupRetentionDays} (default 30 days).
     */
    @Scheduled(fixedDelayString = "${spring.whale.event.retryCleanupScheduleInterval:" + EventProperties.DEFAULT_RETRY_CLEANUP_SCHEDULE_INTERVAL + "}")
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(eventProperties.getRetryCleanupRetentionDays());
        List<EventConsumeFailedRecordEntity> page = recordRepository.findByStatusInAndCreateTimeBefore(
                List.of(EventConsumeStatus.DISCARDED, EventConsumeStatus.REPLAY_SUCCESS, EventConsumeStatus.FINAL_FAILED),
                cutoff,
                PageRequest.of(0, eventProperties.getRetryCleanupBatchSize()));
        if (page.isEmpty()) {
            return;
        }
        log.info("Cleaning up [{}] terminal status records older than {}", page.size(), cutoff);
        recordRepository.deleteAll(page);
    }

}