package com.baskiliisler.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExpiringOffersResponse(
    Integer expiringCount,
    List<ExpiringOffer> expiringOffers,
    BigDecimal totalValue
) {
    public record ExpiringOffer(
        Long id,
        String brandName,
        BigDecimal totalPrice,
        LocalDateTime validUntil
    ) {}
} 