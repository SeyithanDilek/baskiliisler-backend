package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteResponseDto(
        Long id,
        QuoteStatus status,
        BigDecimal totalPrice,
        LocalDate validUntil,
        String brandName,
        LocalDateTime createdAt,
        List<QuoteItemResp> items) {

    public record QuoteItemResp(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal) {}
}