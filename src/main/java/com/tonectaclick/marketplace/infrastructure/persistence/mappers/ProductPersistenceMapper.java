package com.conectaclick.marketplace.infrastructure.persistence.mappers;

import com.conectaclick.marketplace.domain.model.Product;
import com.conectaclick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceMapper {

    public ProductEntity toEntity(Product product) {
        if (product == null) return null;

        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .sellerId(product.getSellerId())
                .status(ProductStatus.valueOf(product.getStatus().name()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product toDomain(ProductEntity entity) {
        if (entity == null) return null;

        return Product.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .sellerId(entity.getSellerId())
                .status(Product.ProductStatus.valueOf(entity.getStatus().name()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
