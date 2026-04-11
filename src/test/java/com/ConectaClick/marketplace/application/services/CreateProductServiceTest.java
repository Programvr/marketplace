package com.ConectaClick.marketplace.application.services;

import com.ConectaClick.marketplace.domain.model.Product;
import com.ConectaClick.marketplace.domain.model.User;
import com.ConectaClick.marketplace.domain.exceptions.UserNotFoundException;
import com.ConectaClick.marketplace.domain.ports.inbound.CreateProductUseCase;
import com.ConectaClick.marketplace.domain.ports.outbound.ProductRepositoryPort;
import com.ConectaClick.marketplace.domain.ports.outbound.UserRepositoryPort;
import com.ConectaClick.marketplace.infrastructure.nosql.services.LoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private CreateProductService createProductService;

    private CreateProductUseCase.CreateProductCommand validCommand;
    private User validSeller;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        validCommand = new CreateProductUseCase.CreateProductCommand(
                "iPhone 15",
                "Latest Apple smartphone",
                new BigDecimal("999.99"),
                10,
                1L
        );

        validSeller = User.builder()
                .id(1L)
                .name("Seller Test")
                .email("seller@test.com")
                .userType(User.UserType.SELLER)
                .build();

        savedProduct = Product.createNew(
                validCommand.name(),
                validCommand.description(),
                validCommand.price(),
                validCommand.stock(),
                validCommand.sellerId()
        );
    }

    @Test
    void shouldCreateProductSuccessfully() {
        // Given
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(validSeller));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(savedProduct);

        // When
        Product result = createProductService.execute(validCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(result.getSellerId()).isEqualTo(1L);

        verify(userRepositoryPort).findById(1L);
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenSellerNotFound() {
        // Given
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> createProductService.execute(validCommand))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with ID: 1");

        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotSeller() {
        // Given
        User buyer = User.builder()
                .id(1L)
                .userType(User.UserType.BUYER)
                .build();
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(buyer));

        // When & Then
        assertThatThrownBy(() -> createProductService.execute(validCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no tiene permisos de vendedor");

        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenPriceIsZero() {
        // Given
        CreateProductUseCase.CreateProductCommand invalidCommand =
                new CreateProductUseCase.CreateProductCommand("Product", "Desc", BigDecimal.ZERO, 10, 1L);

        // When & Then
        assertThatThrownBy(() -> createProductService.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio debe ser mayor a 0,01");
    }

    @Test
    void shouldThrowExceptionWhenStockIsNegative() {
        // Given
        CreateProductUseCase.CreateProductCommand invalidCommand =
                new CreateProductUseCase.CreateProductCommand("Product", "Desc", new BigDecimal("100"), -5, 1L);

        // When & Then
        assertThatThrownBy(() -> createProductService.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El stock no puede ser negativo");
    }
}