package com.ConectaClick.marketplace.domain.ports.inbound;

import com.ConectaClick.marketplace.domain.model.Product;

public interface UpdateProductStockUseCase {
    Product execute(UpdateProductStockCommand command);

    record UpdateProductStockCommand(
            Long productId,
            Integer quantityChange,
            Long sellerId
    ) {}
}
