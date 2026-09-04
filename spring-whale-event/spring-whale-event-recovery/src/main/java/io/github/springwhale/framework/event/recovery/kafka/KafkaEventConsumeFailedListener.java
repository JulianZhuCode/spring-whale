package io.github.springwhale.framework.event.recovery.kafka;

import io.github.springwhale.framework.event.*;
import io.github.springwhale.framework.event.recovery.EventConsumeFailedListener;
import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
public class KafkaEventConsumeFailedListener extends EventConsumeFailedListener {

    public KafkaEventConsumeFailedListener(EventConsumeFailedRecordDao failedRecordDao,
                                           EventProperties eventProperties, ObjectMapper jsonMapper,
                                           RetryStrategyRegistry retryStrategyRegistry,
                                           List<EventMetricsCollector> metricsCollectors,
                                           List<EventConsumeTerminalHandler> terminalHandlers) {
        super(failedRecordDao, eventProperties, jsonMapper, retryStrategyRegistry,
                metricsCollectors, terminalHandlers);
    }

    /**
     * Listener for the failed-event topic. Single consumer group is intentional:
     * horizontal scaling is not needed for processing failed-event records,
     * which are low-volume by nature (only produced on listener exceptions).
     * <p>If processing fails for any reason (e.g. database unavailable), the catch
     * block intentionally does NOT acknowledge the message — Kafka will re-deliver
     * it once the system recovers. This provides at-least-once semantics.</p>
     */
    @KafkaListener(topics = "#{@eventProperties.failedTopic}",
            concurrency = "#{@eventProperties.failedConcurrency}",
            ackMode = "MANUAL_IMMEDIATE",
            groupId = "#{@eventProperties.failedGroupId}")
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
            log.error("Failed to process event message: {}", record.value(), e);
        }
    }

}