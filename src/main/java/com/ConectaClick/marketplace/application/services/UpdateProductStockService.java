package com.ConectaClick.marketplace.application.services;

import com.ConectaClick.marketplace.domain.exceptions.ProductDomainException;
import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.ports.inbound.UpdateProductStockUseCase;
import com.ConectaClick.marketplace.domain.ports.outbound.EventPublisherPort;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductRepositoryPort;
import com.ConectaClick.marketplace.infrastructure.events.StockUpdatedEvent;
import com.ConectaClick.marketplace.infrastructure.nosql.services.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProductStockService implements UpdateProductStockUseCase {

    private final ProductRepositoryPort productRepository;
    private final EventPublisherPort eventPublisher;
    private final LoggingService loggingService;

    @Override
    public Product execute(UpdateProductStockCommand command) {
        // 1. Validar datos de entrada
        validateInput(command);

        // 2. Verificar que el producto existe
        Product existingProduct = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductDomainException("Product not found with id: " + command.productId()));

        // 3. Verificar que el vendedor es el dueño del producto (seguridad)
        if (!existingProduct.getSellerId().equals(command.sellerId())) {
            throw new ProductDomainException("Access denied: Seller is not the owner of this product");
        }

        // 4. Validar reglas de negocio (stock no puede ser negativo)
        int newStock = existingProduct.getStock() + command.quantityChange();
        if (newStock < 0) {
            throw new ProductDomainException("Stock cannot be negative. Current stock: " + existingProduct.getStock() + ", requested change: " + command.quantityChange());
        }

        // 5. Actualizar la entidad Product en el dominio
        Integer previousStock = existingProduct.getStock();
        existingProduct.updateStock(newStock);

        // 6. Persistir en PostgreSQL
        Product updatedProduct = productRepository.save(existingProduct);

        // 7. Guardar log en MongoDB
        String logDetails = String.format("Stock updated for product %s from %d to %d (change: %d)",
                updatedProduct.getName(), previousStock, updatedProduct.getStock(), command.quantityChange());
        loggingService.logActivity(
                command.sellerId(),
                "UPDATE_STOCK",
                "PRODUCT",
                updatedProduct.getId(),
                logDetails,
                "INFO"
        );

        // 8. Publicar evento "StockUpdated"
        StockUpdatedEvent event = StockUpdatedEvent.builder()
                .productId(updatedProduct.getId())
                .productName(updatedProduct.getName())
                .previousStock(previousStock)
                .newStock(updatedProduct.getStock())
                .quantityChange(command.quantityChange())
                .sellerId(updatedProduct.getSellerId())
                .sellerName("") // TODO: Obtener del servicio de usuarios si es necesario
                .sellerEmail("") // TODO: Obtener del servicio de usuarios si es necesario
                .updatedAt(updatedProduct.getUpdatedAt())
                .eventType("StockUpdated")
                .source("marketplace-api")
                .build();

        eventPublisher.publish(event);

        log.info("Stock updated successfully for product {}: {} -> {}",
                updatedProduct.getId(), previousStock, updatedProduct.getStock());

        // 9. Retornar Product actualizado
        return updatedProduct;
    }

    private void validateInput(UpdateProductStockCommand command) {
        if (command.productId() == null || command.productId() <= 0) {
            throw new ProductDomainException("Product ID must be a positive number");
        }
        if (command.quantityChange() == null) {
            throw new ProductDomainException("Quantity change cannot be null");
        }
        if (command.quantityChange() == 0) {
            throw new ProductDomainException("Quantity change cannot be zero");
        }
        if (command.sellerId() == null || command.sellerId() <= 0) {
            throw new ProductDomainException("Seller ID must be a positive number");
        }
    }
}
