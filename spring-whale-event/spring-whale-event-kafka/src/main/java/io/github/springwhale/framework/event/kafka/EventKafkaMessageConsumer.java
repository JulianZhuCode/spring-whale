package io.github.springwhale.framework.event.kafka;

import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import io.github.springwhale.framework.core.utils.ExceptionUtil;
import io.github.springwhale.framework.event.AbstractEventListener;
import io.github.springwhale.framework.event.EventContext;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.springwhale.framework.event.EventProperties.DEFAULT_CONCURRENCY;

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

    @KafkaListener(topics = "#{'${spring.whale.event.listener}'.split(',')}", concurrency = "${spring.whale.event.concurrency:" + DEFAULT_CONCURRENCY + "}", groupId = "${spring.application.name}", properties = {"auto.offset.reset:latest"})
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
            EventMessage message = jsonMapper.readValue(record.value(), EventMessage.class);
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
            log.error("Exception occurred while consuming event message", e);
        }
    }

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
                    kafkaTemplate.send(eventProperties.getFailedTopic(), message.getId(), jsonMapper.writeValueAsString(message)).get(3, TimeUnit.SECONDS);
                }
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
    }
}