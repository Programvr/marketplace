package com.conectaclick.marketplace.infrastructure.events;

import com.conectaclick.marketplace.domain.ports.outbound.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisherAdapter implements EventPublisherPort {

    @Override
    public void publish(Object event) {
        try {
            // TODO: Implementar publicación a Kafka/RabbitMQ cuando se configure
            // Por ahora, solo logueamos el evento
            log.info("Publishing event: {} - {}", event.getClass().getSimpleName(), event);
            
            // Aquí iría la lógica para publicar a un message broker
            // Por ejemplo:
            // kafkaTemplate.send("stock-events", event);
            
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getClass().getSimpleName(), e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }
}
