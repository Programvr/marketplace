package com.ConectaClick.marketplace.infrastructure.events;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductCreatedEvent {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private LocalDateTime createdAt;
    private String eventType;
    private String source;
}