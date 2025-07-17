package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.Unit;
import java.math.BigDecimal;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        Unit unit,
        BigDecimal unitPrice,
        BigDecimal taxRate,
        boolean active
) {} 