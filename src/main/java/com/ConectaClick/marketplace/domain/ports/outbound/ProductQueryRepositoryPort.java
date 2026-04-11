package com.ConectaClick.marketplace.domain.ports.outbound;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.model.Product.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductQueryRepositoryPort {
    // Métodos para paginación y filtros
    List<Product> findBySellerId(Long sellerId, int page, int size);
    List<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, int page, int size);
    List<Product> findBySellerIdAndPriceRange(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);
    List<Product> findBySellerIdAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size);
    
    long countBySellerId(Long sellerId);
    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);
    long countBySellerIdAndPriceRange(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice);
    long countBySellerIdAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate);
}
