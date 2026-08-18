package io.github.springwhale.framework.event.server.kafka;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.server.EventConsumeFailedListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
            log.error("Failed to process event message: {}", record.value(), e);
        }
    }

}