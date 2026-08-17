package io.github.springwhale.framework.event.server;

import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventConsumeFailedListener {
    @Autowired
    protected EventConsumeFailedRecordRepository failedRecordRepository;
}
