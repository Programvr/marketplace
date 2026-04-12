package com.ConectaClick.marketplace.infrastructure.web.controllers;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.ports.inbound.CreateProductUseCase;
import com.ConectaClick.marketplace.domain.ports.inbound.UpdateProductStockUseCase;
import com.ConectaClick.marketplace.infrastructure.web.dto.CreateProductRequest;
import com.ConectaClick.marketplace.infrastructure.web.dto.ProductResponse;
import com.ConectaClick.marketplace.infrastructure.web.dto.UpdateStockRequest;
import com.ConectaClick.marketplace.infrastructure.web.mappers.ProductRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private CreateProductUseCase createProductUseCase;

    @Mock
    private UpdateProductStockUseCase updateProductStockUseCase;

    @Mock
    private ProductRestMapper productRestMapper;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CreateProductRequest validCreateRequest;
    private Product createdProduct;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();

        validCreateRequest = new CreateProductRequest(
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("999.99"),
                10,
                1L
        );

        createdProduct = Product.createNew(
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("999.99"),
                10,
                1L
        );

        productResponse = new ProductResponse(
                1L,
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("999.99"),
                10,
                1L,
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Given
        when(productRestMapper.toCommand(any(CreateProductRequest.class)))
                .thenReturn(new CreateProductUseCase.CreateProductCommand(
                        "iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), 10, 1L));
        when(createProductUseCase.execute(any(CreateProductUseCase.CreateProductCommand.class)))
                .thenReturn(createdProduct);
        when(productRestMapper.toResponse(any(Product.class)))
                .thenReturn(productResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateProductRequestIsInvalid() throws Exception {
        // Given
        CreateProductRequest invalidRequest = new CreateProductRequest(
                "", // Empty name
                "Description",
                BigDecimal.ZERO, // Zero price
                -5, // Negative stock
                null // Null seller ID
        );

        // When & Then
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateProductStockSuccessfully() throws Exception {
        // Given
        UpdateStockRequest updateStockRequest = new UpdateStockRequest(5);
        Product updatedProduct = Product.createNew(
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("999.99"),
                15, // Updated stock
                1L
        );

        when(updateProductStockUseCase.execute(any(UpdateProductStockUseCase.UpdateProductStockCommand.class)))
                .thenReturn(updatedProduct);
        when(productRestMapper.toResponse(any(Product.class)))
                .thenReturn(productResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Seller-Id", "1")
                        .content(objectMapper.writeValueAsString(updateStockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateStockRequestIsInvalid() throws Exception {
        // Given
        UpdateStockRequest invalidRequest = new UpdateStockRequest(null);

        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Seller-Id", "1")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateStockQuantityExceedsLimits() throws Exception {
        // Given
        UpdateStockRequest invalidRequest = new UpdateStockRequest(2000); // Exceeds max limit

        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Seller-Id", "1")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSellerIdHeaderIsMissing() throws Exception {
        // Given
        UpdateStockRequest updateStockRequest = new UpdateStockRequest(5);

        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStockRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSellerIdHeaderIsInvalid() throws Exception {
        // Given
        UpdateStockRequest updateStockRequest = new UpdateStockRequest(5);

        // When & Then
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Seller-Id", "invalid")
                        .content(objectMapper.writeValueAsString(updateStockRequest)))
                .andExpect(status().isBadRequest());
    }
}
