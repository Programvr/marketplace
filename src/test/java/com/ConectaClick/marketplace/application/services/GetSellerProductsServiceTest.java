package com.ConectaClick.marketplace.application.services;

import com.ConectaClick.marketplace.application.dto.PagedProductResponse;
import com.ConectaClick.marketplace.application.dto.ProductDTO;
import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.model.User;
import com.ConectaClick.marketplace.domain.exceptions.UserNotFoundException;
import com.ConectaClick.marketplace.domain.exceptions.UnauthorizedAccessException;
import com.ConectaClick.marketplace.domain.ports.outbound.CachePort;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductQueryRepositoryPort;
import com.ConectaClick.marketplace.domain.ports.outbound.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSellerProductsServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private ProductQueryRepositoryPort productQueryRepositoryPort;

    @Mock
    private CachePort cachePort;

    @InjectMocks
    private GetSellerProductsService getSellerProductsService;

    private User testSeller;
    private List<Product> testProducts;
    private PagedProductResponse testResponse;

    @BeforeEach
    void setUp() {
        testSeller = User.builder()
                .id(1L)
                .email("seller@test.com")
                .name("Test Seller")
                .userType(User.UserType.SELLER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .sellerId(1L)
                .status(Product.ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .stock(20)
                .sellerId(1L)
                .status(Product.ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testProducts = Arrays.asList(product1, product2);

        ProductDTO productDTO1 = ProductDTO.builder()
                .id(1L)
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .sellerId(1L)
                .status("ACTIVE")
                .createdAt(product1.getCreatedAt())
                .updatedAt(product1.getUpdatedAt())
                .build();

        ProductDTO productDTO2 = ProductDTO.builder()
                .id(2L)
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("200.00"))
                .stock(20)
                .sellerId(1L)
                .status("ACTIVE")
                .createdAt(product2.getCreatedAt())
                .updatedAt(product2.getUpdatedAt())
                .build();

        testResponse = PagedProductResponse.builder()
                .products(Arrays.asList(productDTO1, productDTO2))
                .currentPage(0)
                .pageSize(10)
                .totalItems(2)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

    @Test
    void getSellerProducts_Success_WhenUserIsSeller() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;
        int page = 0;
        int size = 10;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(productQueryRepositoryPort.findBySellerId(sellerId, page, size)).thenReturn(testProducts);
        when(productQueryRepositoryPort.countBySellerId(sellerId)).thenReturn(2L);
        when(cachePort.get(anyString(), eq(PagedProductResponse.class))).thenReturn(Optional.empty());

        // When
        PagedProductResponse result = getSellerProductsService.getSellerProducts(sellerId, requestingUserId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getProducts().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
        assertFalse(result.isHasNext());
        assertFalse(result.isHasPrevious());

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort).findBySellerId(sellerId, page, size);
        verify(productQueryRepositoryPort).countBySellerId(sellerId);
        verify(cachePort).put(anyString(), eq(result), any());
    }

    @Test
    void getSellerProducts_ReturnsCachedResult_WhenCacheHit() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;
        int page = 0;
        int size = 10;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(cachePort.get(anyString(), eq(PagedProductResponse.class))).thenReturn(Optional.of(testResponse));

        // When
        PagedProductResponse result = getSellerProductsService.getSellerProducts(sellerId, requestingUserId, page, size);

        // Then
        assertNotNull(result);
        assertEquals(testResponse, result);

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort, never()).findBySellerId(anyLong(), anyInt(), anyInt());
        verify(productQueryRepositoryPort, never()).countBySellerId(anyLong());
        verify(cachePort, never()).put(anyString(), any(), any());
    }

    @Test
    void getSellerProducts_ThrowsException_WhenSellerNotFound() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            getSellerProductsService.getSellerProducts(sellerId, requestingUserId, 0, 10);
        });

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort, never()).findBySellerId(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getSellerProducts_ThrowsException_WhenUnauthorized() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 2L; // Different user

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> {
            getSellerProductsService.getSellerProducts(sellerId, requestingUserId, 0, 10);
        });

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort, never()).findBySellerId(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getSellerProductsByStatus_Success_WithValidStatus() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;
        String status = "ACTIVE";
        int page = 0;
        int size = 10;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(productQueryRepositoryPort.findBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE, page, size))
                .thenReturn(testProducts);
        when(productQueryRepositoryPort.countBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE))
                .thenReturn(2L);
        when(cachePort.get(anyString(), eq(PagedProductResponse.class))).thenReturn(Optional.empty());

        // When
        PagedProductResponse result = getSellerProductsService.getSellerProductsByStatus(sellerId, requestingUserId, status, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getProducts().size());

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort).findBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE, page, size);
        verify(productQueryRepositoryPort).countBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE);
    }

    @Test
    void getSellerProductsByPriceRange_Success_WithValidRange() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("150.00");
        int page = 0;
        int size = 10;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(productQueryRepositoryPort.findBySellerIdAndPriceRange(sellerId, minPrice, maxPrice, page, size))
                .thenReturn(testProducts);
        when(productQueryRepositoryPort.countBySellerIdAndPriceRange(sellerId, minPrice, maxPrice))
                .thenReturn(2L);
        when(cachePort.get(anyString(), eq(PagedProductResponse.class))).thenReturn(Optional.empty());

        // When
        PagedProductResponse result = getSellerProductsService.getSellerProductsByPriceRange(
                sellerId, requestingUserId, minPrice, maxPrice, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getProducts().size());

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort).findBySellerIdAndPriceRange(sellerId, minPrice, maxPrice, page, size);
        verify(productQueryRepositoryPort).countBySellerIdAndPriceRange(sellerId, minPrice, maxPrice);
    }

    @Test
    void getSellerProductsByDateRange_Success_WithValidDateRange() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        int page = 0;
        int size = 10;

        when(userRepositoryPort.findById(sellerId)).thenReturn(Optional.of(testSeller));
        when(productQueryRepositoryPort.findBySellerIdAndDateRange(sellerId, startDate, endDate, page, size))
                .thenReturn(testProducts);
        when(productQueryRepositoryPort.countBySellerIdAndDateRange(sellerId, startDate, endDate))
                .thenReturn(2L);
        when(cachePort.get(anyString(), eq(PagedProductResponse.class))).thenReturn(Optional.empty());

        // When
        PagedProductResponse result = getSellerProductsService.getSellerProductsByDateRange(
                sellerId, requestingUserId, startDate, endDate, page, size);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getProducts().size());

        verify(userRepositoryPort).findById(sellerId);
        verify(productQueryRepositoryPort).findBySellerIdAndDateRange(sellerId, startDate, endDate, page, size);
        verify(productQueryRepositoryPort).countBySellerIdAndDateRange(sellerId, startDate, endDate);
    }

    @Test
    void getSellerProducts_ThrowsException_WithInvalidInput() {
        // Given
        Long sellerId = 1L;
        Long requestingUserId = 1L;

        // When & Then - Negative page
        assertThrows(IllegalArgumentException.class, () -> {
            getSellerProductsService.getSellerProducts(sellerId, requestingUserId, -1, 10);
        });

        // When & Then - Invalid size
        assertThrows(IllegalArgumentException.class, () -> {
            getSellerProductsService.getSellerProducts(sellerId, requestingUserId, 0, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            getSellerProductsService.getSellerProducts(sellerId, requestingUserId, 0, 101);
        });
    }
}
