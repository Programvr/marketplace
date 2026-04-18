package com.conectaclick.marketplace.infrastructure.web.controllers;

import com.conectaclick.marketplace.domain.model.Product;
import com.conectaclick.marketplace.domain.ports.inbound.CreateProductUseCase;
import com.conectaclick.marketplace.domain.ports.inbound.UpdateProductStockUseCase;
import com.conectaclick.marketplace.infrastructure.web.dto.CreateProductRequest;
import com.conectaclick.marketplace.infrastructure.web.dto.ProductResponse;
import com.conectaclick.marketplace.infrastructure.web.dto.UpdateStockRequest;
import com.conectaclick.marketplace.infrastructure.web.mappers.ProductRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductStockUseCase updateProductStockUseCase;
    private final ProductRestMapper productRestMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductUseCase.CreateProductCommand command = productRestMapper.toCommand(request);
        Product createdProduct = createProductUseCase.execute(command);
        ProductResponse response = productRestMapper.toResponse(createdProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequest request,
            @RequestHeader("X-Seller-Id") Long sellerId) {
        
        UpdateProductStockUseCase.UpdateProductStockCommand command = 
                new UpdateProductStockUseCase.UpdateProductStockCommand(
                        productId, 
                        request.quantityChange(), 
                        sellerId
                );
        
        Product updatedProduct = updateProductStockUseCase.execute(command);
        ProductResponse response = productRestMapper.toResponse(updatedProduct);

        return ResponseEntity.ok(response);
    }
}