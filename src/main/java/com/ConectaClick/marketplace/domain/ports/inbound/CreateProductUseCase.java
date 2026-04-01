package com.ConectaClick.marketplace.domain.ports.inbound;

import com.ConectaClick.marketplace.domain.model.Product;

public interface CreateProductUseCase {
    Product execute(CreateProductCommand command);

    record CreateProductCommand(
            String name,
            String description,
            java.math.BigDecimal price,
            Integer stock,
            Long sellerId
    ) {}
}