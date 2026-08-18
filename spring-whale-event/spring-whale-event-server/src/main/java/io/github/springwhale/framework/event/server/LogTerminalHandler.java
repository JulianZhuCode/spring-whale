package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link EventConsumeTerminalHandler} that logs discarded messages at WARN level.
 * <p>Not auto-registered as a Spring bean. To enable, declare it explicitly:</p>
 * <pre>{@code
 * @Bean
 * public LogTerminalHandler logTerminalHandler() {
 *     return new LogTerminalHandler();
 * }
 * }</pre>
 */
@Slf4j
public class LogTerminalHandler implements EventConsumeTerminalHandler {

    @Override
    public void onDiscarded(EventConsumeFailedRecordEntity record) {
        log.warn("Event message discarded: messageId={}, businessName={}, listener={}, retryCount={}",
                record.getMessageId(), record.getBusinessName(), record.getListenerName(), record.getRetryCount());
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}