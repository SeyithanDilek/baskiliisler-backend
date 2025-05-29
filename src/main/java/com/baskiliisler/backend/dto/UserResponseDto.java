package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.common.Role;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Role role
) {
} 