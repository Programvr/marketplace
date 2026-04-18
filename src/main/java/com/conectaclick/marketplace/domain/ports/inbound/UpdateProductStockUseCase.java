package com.conectaclick.marketplace.domain.ports.inbound;

import com.conectaclick.marketplace.domain.model.Product;

public interface UpdateProductStockUseCase {
    Product execute(UpdateProductStockCommand command);

    record UpdateProductStockCommand(
            Long productId,
            Integer quantityChange,
            Long sellerId
    ) {}
}
