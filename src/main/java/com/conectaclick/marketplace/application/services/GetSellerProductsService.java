package com.conectaclick.marketplace.application.services;

import com.conectaclick.marketplace.application.dto.PagedProductResponse;
import com.conectaclick.marketplace.application.dto.ProductDTO;
import com.conectaclick.marketplace.domain.model.Product;
import com.conectaclick.marketplace.domain.model.User;
import com.conectaclick.marketplace.domain.exceptions.UserNotFoundException;
import com.conectaclick.marketplace.domain.exceptions.UnauthorizedAccessException;
import com.conectaclick.marketplace.domain.ports.inbound.GetSellerProductsUseCase;
import com.conectaclick.marketplace.domain.ports.outbound.CachePort;
import com.conectaclick.marketplace.domain.ports.outbound.ProductQueryRepositoryPort;
import com.conectaclick.marketplace.domain.ports.outbound.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que implementa el caso de uso: Obtener Productos del Vendedor
 *
 * Este servicio sigue los principios de Arquitectura Hexagonal:
 * - Depende de puertos (interfaces) no de implementaciones concretas
 * - Contiene la lógica de negocio del caso de uso
 * - Es agnóstico de la infraestructura (base de datos, web, etc.)
 *
 * @author ConectaClick Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetSellerProductsService implements GetSellerProductsUseCase {

    // Puertos (dependencias abstraídas)
    private final UserRepositoryPort userRepositoryPort;
    private final ProductQueryRepositoryPort productQueryRepositoryPort;
    private final CachePort cachePort;

    // Constantes de negocio
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String CACHE_KEY_PREFIX = "seller_products:";

    @Override
    public PagedProductResponse getSellerProducts(Long sellerId, Long requestingUserId, int page, int size) {
        log.info("Getting products for seller {} by user {}", sellerId, requestingUserId);
        
        // Validar datos de entrada
        validateInput(sellerId, requestingUserId, page, size);
        
        // Verificar que el vendedor existe
        User seller = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        
        // Validar reglas de negocio (el vendedor solo puede ver sus propios productos a menos que sea admin)
        validateAuthorization(sellerId, requestingUserId);
        
        // Generar clave de caché
        String cacheKey = generateCacheKey(sellerId, page, size, null, null, null);
        
        // Intentar obtener de caché
        Optional<PagedProductResponse> cachedResult = cachePort.get(cacheKey, PagedProductResponse.class);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for key: {}", cacheKey);
            return cachedResult.get();
        }
        
        // Consultar productos en PostgreSQL con paginación
        List<Product> products = productQueryRepositoryPort.findBySellerId(sellerId, page, size);
        long totalItems = productQueryRepositoryPort.countBySellerId(sellerId);
        
        // Transformar entidades Product a ProductDTO
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Construir respuesta paginada
        PagedProductResponse response = buildPagedResponse(productDTOs, page, size, totalItems);
        
        // Cachear resultados en Redis
        cachePort.put(cacheKey, response, CACHE_TTL);
        log.debug("Cached result for key: {}", cacheKey);
        
        return response;
    }

    @Override
    public PagedProductResponse getSellerProductsByStatus(Long sellerId, Long requestingUserId, 
                                                          String status, int page, int size) {
        log.info("Getting products for seller {} by user {} filtered by status: {}", sellerId, requestingUserId, status);
        
        validateInput(sellerId, requestingUserId, page, size);
        validateStatus(status);
        
        User seller = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        
        validateAuthorization(sellerId, requestingUserId);
        
        Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
        String cacheKey = generateCacheKey(sellerId, page, size, status, null, null);
        
        Optional<PagedProductResponse> cachedResult = cachePort.get(cacheKey, PagedProductResponse.class);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for key: {}", cacheKey);
            return cachedResult.get();
        }
        
        List<Product> products = productQueryRepositoryPort.findBySellerIdAndStatus(sellerId, productStatus, page, size);
        long totalItems = productQueryRepositoryPort.countBySellerIdAndStatus(sellerId, productStatus);
        
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PagedProductResponse response = buildPagedResponse(productDTOs, page, size, totalItems);
        cachePort.put(cacheKey, response, CACHE_TTL);
        
        return response;
    }

    @Override
    public PagedProductResponse getSellerProductsByPriceRange(Long sellerId, Long requestingUserId,
                                                             BigDecimal minPrice, BigDecimal maxPrice, 
                                                             int page, int size) {
        log.info("Getting products for seller {} by user {} filtered by price range: {} - {}", 
                sellerId, requestingUserId, minPrice, maxPrice);
        
        validateInput(sellerId, requestingUserId, page, size);
        validatePriceRange(minPrice, maxPrice);
        
        User seller = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        
        validateAuthorization(sellerId, requestingUserId);
        
        String cacheKey = generateCacheKey(sellerId, page, size, null, minPrice + "-" + maxPrice, null);
        
        Optional<PagedProductResponse> cachedResult = cachePort.get(cacheKey, PagedProductResponse.class);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for key: {}", cacheKey);
            return cachedResult.get();
        }
        
        List<Product> products = productQueryRepositoryPort.findBySellerIdAndPriceRange(
                sellerId, minPrice, maxPrice, page, size);
        long totalItems = productQueryRepositoryPort.countBySellerIdAndPriceRange(sellerId, minPrice, maxPrice);
        
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PagedProductResponse response = buildPagedResponse(productDTOs, page, size, totalItems);
        cachePort.put(cacheKey, response, CACHE_TTL);
        
        return response;
    }

    @Override
    public PagedProductResponse getSellerProductsByDateRange(Long sellerId, Long requestingUserId,
                                                            LocalDateTime startDate, LocalDateTime endDate,
                                                            int page, int size) {
        log.info("Getting products for seller {} by user {} filtered by date range: {} - {}", 
                sellerId, requestingUserId, startDate, endDate);
        
        validateInput(sellerId, requestingUserId, page, size);
        validateDateRange(startDate, endDate);
        
        User seller = userRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        
        validateAuthorization(sellerId, requestingUserId);
        
        String cacheKey = generateCacheKey(sellerId, page, size, null, null, startDate + "-" + endDate);
        
        Optional<PagedProductResponse> cachedResult = cachePort.get(cacheKey, PagedProductResponse.class);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for key: {}", cacheKey);
            return cachedResult.get();
        }
        
        List<Product> products = productQueryRepositoryPort.findBySellerIdAndDateRange(
                sellerId, startDate, endDate, page, size);
        long totalItems = productQueryRepositoryPort.countBySellerIdAndDateRange(sellerId, startDate, endDate);
        
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PagedProductResponse response = buildPagedResponse(productDTOs, page, size, totalItems);
        cachePort.put(cacheKey, response, CACHE_TTL);
        
        return response;
    }

    // Métodos de validación
    private void validateInput(Long sellerId, Long requestingUserId, int page, int size) {
        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("Seller ID must be positive");
        }
        if (requestingUserId == null || requestingUserId <= 0) {
            throw new IllegalArgumentException("Requesting user ID must be positive");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException("Size must be between 1 and " + MAX_SIZE);
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        try {
            Product.ProductStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Valid values are: ACTIVE, INACTIVE, DELETED");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new IllegalArgumentException("Price range values cannot be null");
        }
        if (minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }
        if (maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range values cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private void validateAuthorization(Long sellerId, Long requestingUserId) {
        // Solo permitir acceso si es el mismo vendedor
        // Nota: En un caso real, aquí se verificaría si el usuario es admin
        if (!sellerId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException("User can only view their own products");
        }
    }

    // Métodos de utilidad
    private ProductDTO convertToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .sellerId(product.getSellerId())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private PagedProductResponse buildPagedResponse(List<ProductDTO> products, int page, int size, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        return PagedProductResponse.builder()
                .products(products)
                .currentPage(page)
                .pageSize(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    private String generateCacheKey(Long sellerId, int page, int size, String status, String priceRange, String dateRange) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_KEY_PREFIX)
                .append(sellerId)
                .append(":page=").append(page)
                .append(":size=").append(size);
        
        if (status != null) {
            keyBuilder.append(":status=").append(status);
        }
        if (priceRange != null) {
            keyBuilder.append(":price=").append(priceRange.replace(".", "_"));
        }
        if (dateRange != null) {
            keyBuilder.append(":date=").append(dateRange.replace(":", "_"));
        }
        
        return keyBuilder.toString();
    }
}
