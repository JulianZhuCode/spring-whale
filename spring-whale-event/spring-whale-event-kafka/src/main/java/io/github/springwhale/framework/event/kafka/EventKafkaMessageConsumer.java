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
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventKafkaMessageConsumer extends EventMessageConsumer {
    private final ObjectMapper jsonMapper;
    private final EventProperties eventProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static EventContext getBuildEventContext(ConsumerRecord<String, String> record, EventMessage message) {
        return EventContext.builder()
                .timestamp(record.timestamp())
                .topic(record.topic())
                .authenticationContext(message.getAuthenticationContext())
                .build();
    }

    @KafkaListener(topics = "#{'${spring.whale.event.listener}'.split(',')}", concurrency = "${spring.whale.event.concurrency:1}", groupId = "${spring.application.name}", properties = {"auto.offset.reset:latest"})
    public void listener(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            log.debug("消费事件消息: {}", record.value());
            if (listenerIsEmpty()) {
                return;
            }
            if (record.value() == null) {
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
                    return;
            }
            if (listeners == null) {
                return;
            }
            doListener(record, listeners, message);
        } catch (Exception e) {
            log.error("消费事件消息异常", e);
        } finally {
            ack.acknowledge();
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
                    log.error(String.format("监听器[%s]，消费消息[%s]失败。", listener.getBusinessName(), message.getData()), e);
                    message.setErrorMessage(ExceptionUtil.getStackTrace(e));
                    message.setRetryEnabled(listener.retryEnabled());
                    message.setFailListener(getListenerInstanceToNameMap().get(listener));
                    kafkaTemplate.send(eventProperties.getErrorTopic(), message.getId(), jsonMapper.writeValueAsString(message)).get(3, TimeUnit.SECONDS);
                }
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
    }
}
