package com.baskiliisler.backend.dto;

public record FactoryResponseDto(
        Long id,
        String name,
        String address,
        Integer dailyCapacity,
        boolean active
) {}