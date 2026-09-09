package io.github.springwhale.framework.event.recovery.rabbit;

import com.rabbitmq.client.Channel;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.recovery.EventConsumeFailedListener;
import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
public class RabbitEventConsumeFailedListener extends EventConsumeFailedListener {

    public RabbitEventConsumeFailedListener(EventConsumeFailedRecordDao failedRecordDao,
                                            EventProperties eventProperties, ObjectMapper jsonMapper,
                                            RetryStrategyRegistry retryStrategyRegistry,
                                            List<EventMetricsCollector> metricsCollectors,
                                            List<EventConsumeTerminalHandler> terminalHandlers) {
        super(failedRecordDao, eventProperties, jsonMapper, retryStrategyRegistry,
                metricsCollectors, terminalHandlers);
    }

    /**
     * Listener for the failed-event queue.
     * <p>Processes both FAIL and RETRY_SUCCESS message types. Non-processable messages
     * (EVENT, RETRY) are acknowledged and skipped.</p>
     */
    @RabbitListener(queues = "#{@eventProperties.failedTopic}",
            ackMode = "MANUAL",
            concurrency = "#{@eventProperties.failedConcurrency}")
    public void listenerFailed(String payload, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            EventMessage message = jsonMapper.readValue(payload, EventMessage.class);
            if (!shouldProcess(message.getMessageType())) {
                log.debug("Received non-fail message: {}", message);
                channel.basicAck(deliveryTag, false);
                return;
            }
            handleMessage(message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event message: {}", payload, e);
        }
    }
}