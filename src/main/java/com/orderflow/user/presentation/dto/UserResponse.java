package com.orderflow.user.presentation.dto;

import com.orderflow.user.domain.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {}
