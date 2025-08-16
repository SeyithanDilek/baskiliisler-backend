package com.baskiliisler.backend.dto;

import java.math.BigDecimal;

public record WeeklyOffersResponse(
    Integer acceptedCount,
    Integer declinedCount, // Aktif redler (DECLINED)
    Integer expiredCount,  // Pasif redler (EXPIRED)
    Integer totalRejectedCount, // declinedCount + expiredCount
    Integer sentCount,
    BigDecimal acceptedValue,
    BigDecimal totalValue,
    BigDecimal acceptanceRate
) {} 