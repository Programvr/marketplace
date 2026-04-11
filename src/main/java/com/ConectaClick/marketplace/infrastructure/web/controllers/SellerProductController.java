package com.ConectaClick.marketplace.infrastructure.web.controllers;

import com.ConectaClick.marketplace.application.dto.PagedProductResponse;
import com.ConectaClick.marketplace.domain.ports.inbound.GetSellerProductsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Slf4j
public class SellerProductController {

    private final GetSellerProductsUseCase getSellerProductsUseCase;

    @GetMapping("/{sellerId}/products")
    public ResponseEntity<PagedProductResponse> getSellerProducts(
            @PathVariable Long sellerId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting products for seller {} by user {}", sellerId, requestingUserId);
        
        PagedProductResponse response = getSellerProductsUseCase.getSellerProducts(
                sellerId, requestingUserId, page, size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sellerId}/products/by-status")
    public ResponseEntity<PagedProductResponse> getSellerProductsByStatus(
            @PathVariable Long sellerId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting products for seller {} by user {} filtered by status: {}", 
                sellerId, requestingUserId, status);
        
        PagedProductResponse response = getSellerProductsUseCase.getSellerProductsByStatus(
                sellerId, requestingUserId, status, page, size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sellerId}/products/by-price-range")
    public ResponseEntity<PagedProductResponse> getSellerProductsByPriceRange(
            @PathVariable Long sellerId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting products for seller {} by user {} filtered by price range: {} - {}", 
                sellerId, requestingUserId, minPrice, maxPrice);
        
        PagedProductResponse response = getSellerProductsUseCase.getSellerProductsByPriceRange(
                sellerId, requestingUserId, minPrice, maxPrice, page, size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sellerId}/products/by-date-range")
    public ResponseEntity<PagedProductResponse> getSellerProductsByDateRange(
            @PathVariable Long sellerId,
            @RequestHeader("X-User-Id") Long requestingUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting products for seller {} by user {} filtered by date range: {} - {}", 
                sellerId, requestingUserId, startDate, endDate);
        
        PagedProductResponse response = getSellerProductsUseCase.getSellerProductsByDateRange(
                sellerId, requestingUserId, startDate, endDate, page, size);
        
        return ResponseEntity.ok(response);
    }
}
