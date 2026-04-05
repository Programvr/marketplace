package com.ConectaClick.marketplace.infrastructure.events;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class StockUpdatedEvent {
    private Long productId;
    private String productName;
    private Integer previousStock;
    private Integer newStock;
    private Integer quantityChange;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private LocalDateTime updatedAt;
    private String eventType;
    private String source;
}
