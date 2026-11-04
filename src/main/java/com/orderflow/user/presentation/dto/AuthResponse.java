package com.orderflow.user.presentation.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {}