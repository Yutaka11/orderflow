package com.orderflow.catalog.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID categoryId,
        String name,
        String description,
        String sku,
        BigDecimal price,
        int stockQuantity,
        boolean active
) {
}