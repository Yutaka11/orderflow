package com.orderflow.catalog.presentation.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        boolean active
) {
}