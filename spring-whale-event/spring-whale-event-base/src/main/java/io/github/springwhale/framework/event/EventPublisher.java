package io.github.springwhale.framework.event;

import jakarta.validation.Valid;

public abstract class EventPublisher {

    abstract void publish(@Valid Object event);

    abstract void publish(@Valid Object event, PublishOption option);

    abstract void publish(@Valid EventMessage message);

    abstract void publish(@Valid EventMessage message, PublishOption option);

}
