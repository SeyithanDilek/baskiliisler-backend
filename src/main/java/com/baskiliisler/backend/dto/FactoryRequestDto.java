package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record FactoryRequestDto(
        @NotBlank String name,
        String address,
        Integer dailyCapacity,     // null = bilinmiyor
        Boolean active             // PATCH’te opsiyonel
) {}