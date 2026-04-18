package com.conectaclick.marketplace.domain.ports.outbound;

public interface EventPublisherPort {
    void publish(Object event);
}
