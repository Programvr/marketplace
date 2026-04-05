package com.ConectaClick.marketplace.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record UpdateStockRequest(
        @NotNull(message = "Quantity change cannot be null")
        @Min(value = -1000, message = "Quantity change cannot be less than -1000")
        @Max(value = 1000, message = "Quantity change cannot be greater than 1000")
        Integer quantityChange
) {}
