package com.ConectaClick.marketplace.infrastructure.persistence.repositories;

import com.ConectaClick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.ConectaClick.marketplace.infrastructure.persistence.entities.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {
    
    // Métodos para consultas básicas con paginación
    List<ProductEntity> findBySellerId(Long sellerId, Pageable pageable);
    
    // Métodos para consultas con filtros
    List<ProductEntity> findBySellerIdAndStatus(Long sellerId, ProductStatus status, Pageable pageable);
    List<ProductEntity> findBySellerIdAndPriceBetween(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    List<ProductEntity> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Métodos de conteo
    long countBySellerId(Long sellerId);
    long countBySellerIdAndStatus(Long sellerId, ProductStatus status);
    long countBySellerIdAndPriceBetween(Long sellerId, BigDecimal minPrice, BigDecimal maxPrice);
    long countBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Consulta personalizada para productos activos (opcional)
    @Query("SELECT p FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.status = 'ACTIVE'")
    List<ProductEntity> findActiveProductsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}
