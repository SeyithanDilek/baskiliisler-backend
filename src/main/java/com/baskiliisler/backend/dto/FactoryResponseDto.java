package com.baskiliisler.backend.dto;

public record FactoryResponseDto(
        Long id,
        String name,
        String address,
        String phoneNumber,
        boolean active
) {}