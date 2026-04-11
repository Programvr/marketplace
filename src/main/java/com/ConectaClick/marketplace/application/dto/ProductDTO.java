package com.ConectaClick.marketplace.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long sellerId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
