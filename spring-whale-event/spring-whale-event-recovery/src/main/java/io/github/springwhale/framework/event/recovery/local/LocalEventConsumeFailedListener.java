package io.github.springwhale.framework.event.recovery.local;

import io.github.springwhale.framework.event.*;
import io.github.springwhale.framework.event.recovery.EventConsumeFailedListener;
import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
public class LocalEventConsumeFailedListener extends EventConsumeFailedListener {

    public LocalEventConsumeFailedListener(EventConsumeFailedRecordDao failedRecordDao,
                                           EventProperties eventProperties, ObjectMapper jsonMapper,
                                           RetryStrategyRegistry retryStrategyRegistry,
                                           List<EventMetricsCollector> metricsCollectors,
                                           List<EventConsumeTerminalHandler> terminalHandlers) {
        super(failedRecordDao, eventProperties, jsonMapper, retryStrategyRegistry,
                metricsCollectors, terminalHandlers);
    }

    /**
     * Listener for the local failed event.
     * <p>Receives FAIL and RETRY_SUCCESS type {@link EventMessage} via Spring's event mechanism.
     * The message is persisted to the database for retry processing by
     * {@link io.github.springwhale.framework.event.recovery.EventRetryTask}.</p>
     * <p>Non-processable messages (EVENT, RETRY) are silently ignored. This listener runs synchronously
     * on the caller's thread (which is already an async executor thread from
     * {@code LocalEventMessageConsumer}), matching the Kafka consumer pattern
     * where the failed-topic listener runs in its own Kafka thread.</p>
     */
    @EventListener
    public void onFailedEvent(EventMessage message) {
        if (!shouldProcess(message.getMessageType())) {
            return;
        }
        log.debug("Consuming local failed event message: {}", message.getData());
        handleMessage(message);
    }

}