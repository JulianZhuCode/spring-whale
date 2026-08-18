package io.github.springwhale.framework.event.server.rabbit;

import com.rabbitmq.client.Channel;
import io.github.springwhale.framework.event.*;
import io.github.springwhale.framework.event.server.EventConsumeFailedListener;
import io.github.springwhale.framework.event.server.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.server.dao.EventConsumeFailedRecordDao;
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

    @RabbitListener(queues = "#{@eventProperties.failedTopic}",
            concurrency = "#{@eventProperties.failedConcurrency}")
    public void listenerFailed(String payload, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            EventMessage message = jsonMapper.readValue(payload, EventMessage.class);
            if (message.getMessageType() != MessageType.FAIL) {
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