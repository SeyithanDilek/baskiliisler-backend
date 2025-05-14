package com.baskiliisler.backend.dto;

public record BrandResponseDto(
        Long id,
        String name,
        String contactEmail,
        String contactPhone
) {}
