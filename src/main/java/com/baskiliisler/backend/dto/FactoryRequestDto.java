package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record FactoryRequestDto(
        @NotBlank String name,
        String address,
        String phoneNumber,
        Boolean active             // PATCH'te opsiyonel
) {}