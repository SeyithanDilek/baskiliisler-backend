package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record QuoteItemRequestDto(
        @NotNull Long productId,
        @Min(1)  Integer quantity,
        @Positive BigDecimal unitPrice) {}