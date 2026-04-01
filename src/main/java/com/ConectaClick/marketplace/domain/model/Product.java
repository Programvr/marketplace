package com.ConectaClick.marketplace.domain.model;

import com.ConectaClick.marketplace.domain.exceptions.ProductDomainException;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long sellerId;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ProductStatus {
        ACTIVE, INACTIVE, DELETED
    }

    // Constructor para crear nuevo producto (sin ID)
    public static Product createNew(String name, String description,
                                    BigDecimal price, Integer stock, Long sellerId) {
        validate(name, price, stock, sellerId);

        return Product.builder()
                .name(name.trim())
                .description(description)
                .price(price)
                .stock(stock)
                .sellerId(sellerId)
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static void validate(String name, BigDecimal price, Integer stock, Long sellerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new ProductDomainException("Product name cannot be empty");
        }
        if (name.length() > 200) {
            throw new ProductDomainException("Product name cannot exceed 200 characters");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductDomainException("Price must be greater than zero");
        }
        if (stock == null || stock < 0) {
            throw new ProductDomainException("Stock cannot be negative");
        }
        if (sellerId == null || sellerId <= 0) {
            throw new ProductDomainException("Invalid seller ID");
        }
    }

    // Métodos de negocio
    public void updateStock(int newStock) {
        if (newStock < 0) {
            throw new ProductDomainException("Stock cannot be negative");
        }
        this.stock = newStock;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
}
