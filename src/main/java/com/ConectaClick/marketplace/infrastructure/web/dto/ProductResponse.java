package com.conectaclick.marketplace.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Long sellerId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}