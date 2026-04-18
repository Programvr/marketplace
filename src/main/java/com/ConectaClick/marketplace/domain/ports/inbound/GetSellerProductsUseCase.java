package com.conectaclick.marketplace.domain.ports.inbound;

import com.conectaclick.marketplace.application.dto.PagedProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface GetSellerProductsUseCase {
    
    PagedProductResponse getSellerProducts(Long sellerId, Long requestingUserId, int page, int size);
    
    PagedProductResponse getSellerProductsByStatus(Long sellerId, Long requestingUserId, 
                                                  String status, int page, int size);
    
    PagedProductResponse getSellerProductsByPriceRange(Long sellerId, Long requestingUserId,
                                                       BigDecimal minPrice, BigDecimal maxPrice, 
                                                       int page, int size);
    
    PagedProductResponse getSellerProductsByDateRange(Long sellerId, Long requestingUserId,
                                                      LocalDateTime startDate, LocalDateTime endDate,
                                                      int page, int size);
}
