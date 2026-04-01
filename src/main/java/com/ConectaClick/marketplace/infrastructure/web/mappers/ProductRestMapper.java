package com.ConectaClick.marketplace.infrastructure.web.mappers;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.infrastructure.web.dto.CreateProductRequest;
import com.ConectaClick.marketplace.infrastructure.web.dto.ProductResponse;
import com.ConectaClick.marketplace.domain.ports.inbound.CreateProductUseCase;
import org.springframework.stereotype.Component;

@Component
public class ProductRestMapper {

    public CreateProductUseCase.CreateProductCommand toCommand(CreateProductRequest request) {
        return new CreateProductUseCase.CreateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.sellerId()
        );
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getSellerId(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}