package com.baskiliisler.backend.dto;

public record AuthResponseDto(
        String token,
        String type,
        UserResponseDto user
) {
    public AuthResponseDto(String token, UserResponseDto user) {
        this(token, "Bearer", user);
    }
} 