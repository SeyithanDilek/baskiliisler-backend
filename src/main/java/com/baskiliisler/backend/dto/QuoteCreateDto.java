package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record QuoteCreateDto(
        @NotNull Long brandId,
        @NotEmpty List<QuoteItemRequestDto> items,
        @Future LocalDate validUntil) {}