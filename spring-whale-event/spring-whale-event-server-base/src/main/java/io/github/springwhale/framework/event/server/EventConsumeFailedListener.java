package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.server.entity.EventConsumeFailedRecordEntity;
import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import io.github.springwhale.framework.event.server.util.EventFailedRecordIdGenerator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.ObjectMapper;

public abstract class EventConsumeFailedListener {
    @Autowired
    protected EventConsumeFailedRecordRepository failedRecordRepository;
    @Autowired
    protected EventProperties eventProperties;
    @Autowired
    protected ObjectMapper jsonMapper;

    protected @NonNull EventConsumeFailedRecordEntity buildRecordEntity(EventMessage message) {
        EventConsumeFailedRecordEntity entity = new EventConsumeFailedRecordEntity();
        entity.setId(EventFailedRecordIdGenerator.generate(message.getId(), message.getFailListener()));
        entity.setMessageId(message.getId());
        entity.setSource(message.getSource());
        entity.setBusinessName(message.getBusinessName());
        entity.setListenerName(message.getFailListener());
        entity.setAuthenticationContext(jsonMapper.writeValueAsString(message.getAuthenticationContext()));
        entity.setTopic(message.getTopic());
        entity.setRawMessage(message.getData());
        entity.setErrorStack(message.getErrorStack());
        return entity;
    }
}