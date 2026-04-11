package com.ConectaClick.marketplace.infrastructure.persistence.adapters;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.model.Product.ProductStatus;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductQueryRepositoryPort;
import com.ConectaClick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.ConectaClick.marketplace.infrastructure.persistence.mappers.ProductPersistenceMapper;
import com.ConectaClick.marketplace.infrastructure.persistence.repositories.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductQueryRepositoryAdapter implements ProductQueryRepositoryPort {

    private final JpaProductRepository jpaProductRepository;
    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public List<Product> findBySellerId(Long sellerId, int page, int size) {
        log.debug("Finding products for seller {} with page {} and size {}", sellerId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        List<ProductEntity> entities = jpaProductRepository.findBySellerId(sellerId, pageable);
        
        return entities.stream()
                .map(productPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findBySellerIdAndStatus(Long sellerId, ProductStatus status, int page, int size) {
        log.debug("Finding products for seller {} with status {} and page {} size {}", 
                sellerId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        // Convertir ProductStatus de dominio a infraestructura
        com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.ProductStatus infrastructureStatus = 
                com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.valueOf(status.name());
        List<ProductEntity> entities = jpaProductRepository.findBySellerIdAndStatus(sellerId, infrastructureStatus, pageable);
        
        return entities.stream()
                .map(productPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findBySellerIdAndPriceRange(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        log.debug("Finding products for seller {} with price range {} - {} and page {} size {}", 
                sellerId, minPrice, maxPrice, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        List<ProductEntity> entities = jpaProductRepository.findBySellerIdAndPriceBetween(
                sellerId, minPrice, maxPrice, pageable);
        
        return entities.stream()
                .map(productPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findBySellerIdAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        log.debug("Finding products for seller {} with date range {} - {} and page {} size {}", 
                sellerId, startDate, endDate, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        List<ProductEntity> entities = jpaProductRepository.findBySellerIdAndCreatedAtBetween(
                sellerId, startDate, endDate, pageable);
        
        return entities.stream()
                .map(productPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countBySellerId(Long sellerId) {
        log.debug("Counting products for seller {}", sellerId);
        return jpaProductRepository.countBySellerId(sellerId);
    }

    @Override
    public long countBySellerIdAndStatus(Long sellerId, ProductStatus status) {
        log.debug("Counting products for seller {} with status {}", sellerId, status);
        // Convertir ProductStatus de dominio a infraestructura
        com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.ProductStatus infrastructureStatus = 
                com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.valueOf(status.name());
        return jpaProductRepository.countBySellerIdAndStatus(sellerId, infrastructureStatus);
    }

    @Override
    public long countBySellerIdAndPriceRange(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Counting products for seller {} with price range {} - {}", sellerId, minPrice, maxPrice);
        return jpaProductRepository.countBySellerIdAndPriceBetween(sellerId, minPrice, maxPrice);
    }

    @Override
    public long countBySellerIdAndDateRange(Long sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Counting products for seller {} with date range {} - {}", sellerId, startDate, endDate);
        return jpaProductRepository.countBySellerIdAndCreatedAtBetween(sellerId, startDate, endDate);
    }
}
