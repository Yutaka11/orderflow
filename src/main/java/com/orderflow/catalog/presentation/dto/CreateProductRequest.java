package com.orderflow.catalog.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        String description,

        @NotBlank(message = "SKU is required")
        @Size(max = 60)
        String sku,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity
) {
}