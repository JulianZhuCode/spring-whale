package io.github.springwhale.framework.event;

import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public abstract class EventPublisher {

    @Getter
    @Value("${spring.whale.event.default-topic:DEFAULT_TOPIC}")
    private String defaultTopic;

    abstract void publish(@Valid Object event);

    abstract void publish(@Valid Object event, PublishOption option);

    abstract void publish(@Valid EventMessage message);

    abstract void publish(@Valid EventMessage message, PublishOption option);

}
