package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.ExceptionUtil;
import io.github.springwhale.framework.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaMessageConsumer extends EventMessageConsumer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static EventContext getBuildEventContext(ConsumerRecord<String, String> record, EventMessage message) {
        return EventContext.builder()
                .timestamp(record.timestamp())
                .topic(record.topic())
                .authenticationContext(message.getAuthenticationContext())
                .build();
    }

    /**
     * Main Kafka event listener.
     * <p>Consumes messages from the configured topic(s) with manual acknowledgment.
     * The outer catch block intentionally does NOT acknowledge the message, so that
     * Kafka will re-deliver it — this is by design for at-least-once semantics.
     * The only truly unrecoverable scenario (deserialization failure) is handled
     * in the inner catch block where the message is acknowledged and discarded.</p>
     */
    @KafkaListener(topics = "#{@eventProperties.listener.split(',')}",
            concurrency = "#{@kafkaEventProperties.concurrency}",
            groupId = "${spring.application.name}",
            properties = {"#{'auto.offset.reset:' + @kafkaEventProperties.autoOffsetReset}"})
    public void listener(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.debug("Consuming event message: {}", record.value());
            if (listenerIsEmpty()) {
                ack.acknowledge();
                return;
            }
            if (record.value() == null) {
                ack.acknowledge();
                return;
            }
            EventMessage message = null;
            try {
                message = jsonMapper.readValue(record.value(), EventMessage.class);
            } catch (JacksonException e) {
                log.error("Failed to deserialize event message: {}", record.value(), e);
                ack.acknowledge();
                return;
            }
            List<AbstractEventListener<?>> listeners;
            switch (message.getMessageType()) {
                case EVENT:
                    listeners = getListenerGroup().get(message.getBusinessName());
                    break;
                case RETRY:
                    listeners = Collections.singletonList(getListenerNameToInstanceMap().get(message.getFailListener()));
                    break;
                default:
                    ack.acknowledge();
                    return;
            }
            if (listeners == null) {
                ack.acknowledge();
                return;
            }
            doListener(record, listeners, message);
            ack.acknowledge();
        } catch (Exception e) {
            // Intentionally do NOT acknowledge here: at-least-once semantics.
            // If processing fails (e.g. database or downstream unavailable),
            // Kafka will re-deliver the message once the system recovers.
            log.error("Exception occurred while consuming event message", e);
        }
    }

    /**
     * Dispatch the message to each matching listener.
     * <p>Each listener failure is handled independently: the exception is caught per-listener,
     * the error info is recorded on the message, and the message is immediately sent to the
     * failed topic for retry processing. If multiple listeners fail, each failure
     * sends its own copy to the failed topic.</p>
     * <p>The Kafka send to the failed topic is synchronous (blocking with timeout). This is
     * acceptable because listener failures are low-probability events and the blocking
     * duration is bounded by {@code sendTimeoutSeconds}.</p>
     * <p>Authentication context is set on the current thread before dispatching (if present
     * on the message) and cleared in the finally block, ensuring no cross-message context leakage.</p>
     */
    private void doListener(ConsumerRecord<String, String> record, List<AbstractEventListener<?>> listeners, EventMessage message) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            if (message.getAuthenticationContext() != null) {
                AuthenticationContextHolder.setContext(message.getAuthenticationContext());
            }
            for (AbstractEventListener<?> listener : listeners) {
                try {
                    var event = jsonMapper.readValue(message.getData(), listener.getEventClass());
                    listener.onEvent(event, getBuildEventContext(record, message));
                } catch (Exception e) {
                    log.error("Listener [{}] failed to consume message [{}].", listener.getBusinessName(), message.getData(), e);
                    message.setErrorStack(ExceptionUtil.getStackTrace(e));
                    message.setRetryEnabled(listener.retryEnabled());
                    message.setFailListener(getListenerInstanceToNameMap().get(listener));
                    message.setMessageType(MessageType.FAIL);
                    kafkaTemplate.send(eventProperties.getFailedTopic(), message.getId(), jsonMapper.writeValueAsString(message)).get(eventProperties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
                }
                if (MessageType.RETRY == message.getMessageType()) {
                    message.setRetrySuccess(true);
                    kafkaTemplate.send(eventProperties.getFailedTopic(), message.getId(), jsonMapper.writeValueAsString(message)).get(eventProperties.getSendTimeoutSeconds(), TimeUnit.SECONDS);
                }
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
    }
}