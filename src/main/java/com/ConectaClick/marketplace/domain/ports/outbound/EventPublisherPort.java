package com.ConectaClick.marketplace.domain.ports.outbound;

public interface EventPublisherPort {
    void publish(Object event);
}
