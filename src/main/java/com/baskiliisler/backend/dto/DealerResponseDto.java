package com.baskiliisler.backend.dto;

public record DealerResponseDto(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String taxNumber,
        boolean active
) {
} 