package com.baskiliisler.backend.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long id,
        String code,
        String name,
        String unit,
        BigDecimal unitPrice,
        boolean active
) {} 