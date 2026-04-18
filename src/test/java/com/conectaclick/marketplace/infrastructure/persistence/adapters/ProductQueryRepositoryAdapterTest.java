package com.conectaclick.marketplace.infrastructure.persistence.adapters;

import com.conectaclick.marketplace.domain.model.Product;
import com.conectaclick.marketplace.domain.model.Product.ProductStatus;
import com.conectaclick.marketplace.infrastructure.persistence.entities.ProductEntity;
import com.conectaclick.marketplace.infrastructure.persistence.mappers.ProductPersistenceMapper;
import com.conectaclick.marketplace.infrastructure.persistence.repositories.JpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductQueryRepositoryAdapterTest {

    @Mock
    private JpaProductRepository jpaProductRepository;

    @Mock
    private ProductPersistenceMapper productPersistenceMapper;

    @InjectMocks
    private ProductQueryRepositoryAdapter adapter;

    private ProductEntity testProductEntity;
    private Product testProduct;
    private final Long sellerId = 1L;
    private final int page = 0;
    private final int size = 10;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testProductEntity = ProductEntity.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .stock(50)
                .sellerId(sellerId)
                .status(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .stock(50)
                .sellerId(sellerId)
                .status(ProductStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void shouldFindProductsBySellerId() {
        // Given
        List<ProductEntity> entities = List.of(testProductEntity);
        List<Product> expectedProducts = List.of(testProduct);
        
        when(jpaProductRepository.findBySellerId(eq(sellerId), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);

        // When
        List<Product> result = adapter.findBySellerId(sellerId, page, size);

        // Then
        assertEquals(expectedProducts.size(), result.size());
        assertEquals(expectedProducts.get(0).getId(), result.get(0).getId());
        assertEquals(expectedProducts.get(0).getName(), result.get(0).getName());
        
        verify(jpaProductRepository).findBySellerId(eq(sellerId), any(Pageable.class));
        verify(productPersistenceMapper).toDomain(testProductEntity);
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsFoundBySellerId() {
        // Given
        List<ProductEntity> entities = List.of();
        
        when(jpaProductRepository.findBySellerId(eq(sellerId), any(Pageable.class)))
                .thenReturn(entities);

        // When
        List<Product> result = adapter.findBySellerId(sellerId, page, size);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaProductRepository).findBySellerId(eq(sellerId), any(Pageable.class));
        verify(productPersistenceMapper, never()).toDomain(any());
    }

    @Test
    void shouldFindProductsBySellerIdAndStatus() {
        // Given
        ProductStatus status = ProductStatus.ACTIVE;
        List<ProductEntity> entities = List.of(testProductEntity);
        List<Product> expectedProducts = List.of(testProduct);
        
        when(jpaProductRepository.findBySellerIdAndStatus(eq(sellerId), eq(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);

        // When
        List<Product> result = adapter.findBySellerIdAndStatus(sellerId, status, page, size);

        // Then
        assertEquals(expectedProducts.size(), result.size());
        assertEquals(expectedProducts.get(0).getId(), result.get(0).getId());
        
        verify(jpaProductRepository).findBySellerIdAndStatus(eq(sellerId), eq(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.ACTIVE), any(Pageable.class));
        verify(productPersistenceMapper).toDomain(testProductEntity);
    }

    @Test
    void shouldFindProductsBySellerIdAndPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        List<ProductEntity> entities = List.of(testProductEntity);
        List<Product> expectedProducts = List.of(testProduct);
        
        when(jpaProductRepository.findBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);

        // When
        List<Product> result = adapter.findBySellerIdAndPriceRange(sellerId, minPrice, maxPrice, page, size);

        // Then
        assertEquals(expectedProducts.size(), result.size());
        assertEquals(expectedProducts.get(0).getId(), result.get(0).getId());
        
        verify(jpaProductRepository).findBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice), any(Pageable.class));
        verify(productPersistenceMapper).toDomain(testProductEntity);
    }

    @Test
    void shouldFindProductsBySellerIdAndDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<ProductEntity> entities = List.of(testProductEntity);
        List<Product> expectedProducts = List.of(testProduct);
        
        when(jpaProductRepository.findBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);

        // When
        List<Product> result = adapter.findBySellerIdAndDateRange(sellerId, startDate, endDate, page, size);

        // Then
        assertEquals(expectedProducts.size(), result.size());
        assertEquals(expectedProducts.get(0).getId(), result.get(0).getId());
        
        verify(jpaProductRepository).findBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate), any(Pageable.class));
        verify(productPersistenceMapper).toDomain(testProductEntity);
    }

    @Test
    void shouldCountProductsBySellerId() {
        // Given
        long expectedCount = 5L;
        
        when(jpaProductRepository.countBySellerId(eq(sellerId)))
                .thenReturn(expectedCount);

        // When
        long result = adapter.countBySellerId(sellerId);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaProductRepository).countBySellerId(eq(sellerId));
    }

    @Test
    void shouldCountProductsBySellerIdAndStatus() {
        // Given
        ProductStatus status = ProductStatus.INACTIVE;
        long expectedCount = 3L;
        
        when(jpaProductRepository.countBySellerIdAndStatus(eq(sellerId), eq(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.INACTIVE)))
                .thenReturn(expectedCount);

        // When
        long result = adapter.countBySellerIdAndStatus(sellerId, status);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaProductRepository).countBySellerIdAndStatus(eq(sellerId), eq(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.INACTIVE));
    }

    @Test
    void shouldCountProductsBySellerIdAndPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("500.00");
        long expectedCount = 8L;
        
        when(jpaProductRepository.countBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice)))
                .thenReturn(expectedCount);

        // When
        long result = adapter.countBySellerIdAndPriceRange(sellerId, minPrice, maxPrice);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaProductRepository).countBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice));
    }

    @Test
    void shouldCountProductsBySellerIdAndDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        long expectedCount = 12L;
        
        when(jpaProductRepository.countBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate)))
                .thenReturn(expectedCount);

        // When
        long result = adapter.countBySellerIdAndDateRange(sellerId, startDate, endDate);

        // Then
        assertEquals(expectedCount, result);
        verify(jpaProductRepository).countBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate));
    }

    @Test
    void shouldHandleMultipleProductsInFindOperations() {
        // Given
        ProductEntity entity2 = ProductEntity.builder()
                .id(2L)
                .name("Test Product 2")
                .description("Test Description 2")
                .price(new BigDecimal("200.00"))
                .stock(30)
                .sellerId(sellerId)
                .status(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Test Product 2")
                .description("Test Description 2")
                .price(new BigDecimal("200.00"))
                .stock(30)
                .sellerId(sellerId)
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<ProductEntity> entities = List.of(testProductEntity, entity2);
        
        when(jpaProductRepository.findBySellerId(eq(sellerId), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);
        when(productPersistenceMapper.toDomain(entity2))
                .thenReturn(product2);

        // When
        List<Product> result = adapter.findBySellerId(sellerId, page, size);

        // Then
        assertEquals(2, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        assertEquals(product2.getId(), result.get(1).getId());
        
        verify(productPersistenceMapper, times(2)).toDomain(any());
    }

    @Test
    void shouldHandleNullStatusInFindOperations() {
        // Given
        List<ProductEntity> entities = List.of(testProductEntity);
        
        when(jpaProductRepository.findBySellerId(eq(sellerId), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(testProduct);

        // When
        List<Product> result = adapter.findBySellerId(sellerId, page, size);

        // Then
        assertFalse(result.isEmpty());
        verify(jpaProductRepository).findBySellerId(eq(sellerId), any(Pageable.class));
    }

    @Test
    void shouldHandleEmptyPriceRange() {
        // Given
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;
        List<ProductEntity> entities = List.of();
        
        when(jpaProductRepository.findBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice), any(Pageable.class)))
                .thenReturn(entities);

        // When
        List<Product> result = adapter.findBySellerIdAndPriceRange(sellerId, minPrice, maxPrice, page, size);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaProductRepository).findBySellerIdAndPriceBetween(eq(sellerId), eq(minPrice), eq(maxPrice), any(Pageable.class));
    }

    @Test
    void shouldHandleEmptyDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1); // Invalid range
        List<ProductEntity> entities = List.of();
        
        when(jpaProductRepository.findBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(entities);

        // When
        List<Product> result = adapter.findBySellerIdAndDateRange(sellerId, startDate, endDate, page, size);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaProductRepository).findBySellerIdAndCreatedAtBetween(eq(sellerId), eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    void shouldConvertAllStatusEnumsCorrectly() {
        // Given
        for (ProductStatus domainStatus : ProductStatus.values()) {
            List<ProductEntity> entities = List.of(testProductEntity);
            com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus infraStatus = com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.valueOf(domainStatus.name());
            
            when(jpaProductRepository.findBySellerIdAndStatus(eq(sellerId), eq(infraStatus), any(Pageable.class)))
                    .thenReturn(entities);
            when(productPersistenceMapper.toDomain(testProductEntity))
                    .thenReturn(testProduct);

            // When
            List<Product> result = adapter.findBySellerIdAndStatus(sellerId, domainStatus, page, size);

            // Then
            assertFalse(result.isEmpty());
            verify(jpaProductRepository).findBySellerIdAndStatus(eq(sellerId), eq(infraStatus), any(Pageable.class));
        }
    }

    @Test
    void shouldHandleZeroCountResults() {
        // Given
        when(jpaProductRepository.countBySellerId(eq(sellerId)))
                .thenReturn(0L);
        when(jpaProductRepository.countBySellerIdAndStatus(eq(sellerId), eq(com.conectaclick.marketplace.infrastructure.persistence.entities.enums.ProductStatus.ACTIVE)))
                .thenReturn(0L);
        when(jpaProductRepository.countBySellerIdAndPriceBetween(eq(sellerId), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(0L);
        when(jpaProductRepository.countBySellerIdAndCreatedAtBetween(eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        // When & Then
        assertEquals(0L, adapter.countBySellerId(sellerId));
        assertEquals(0L, adapter.countBySellerIdAndStatus(sellerId, ProductStatus.ACTIVE));
        assertEquals(0L, adapter.countBySellerIdAndPriceRange(sellerId, BigDecimal.ZERO, BigDecimal.TEN));
        assertEquals(0L, adapter.countBySellerIdAndDateRange(sellerId, LocalDateTime.now(), LocalDateTime.now()));
    }

    @Test
    void shouldHandleNullMapperResult() {
        // Given
        List<ProductEntity> entities = List.of(testProductEntity);
        
        when(jpaProductRepository.findBySellerId(eq(sellerId), any(Pageable.class)))
                .thenReturn(entities);
        when(productPersistenceMapper.toDomain(testProductEntity))
                .thenReturn(null);

        // When
        List<Product> result = adapter.findBySellerId(sellerId, page, size);

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0));
        verify(productPersistenceMapper).toDomain(testProductEntity);
    }
}
