package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record QuoteUpdateDto(
        @NotEmpty List<QuoteItemRequestDto> items,
        LocalDate validUntil) {}