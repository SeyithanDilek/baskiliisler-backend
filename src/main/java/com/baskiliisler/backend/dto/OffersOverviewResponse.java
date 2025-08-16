package com.baskiliisler.backend.dto;

import java.math.BigDecimal;

public record OffersOverviewResponse(
    Integer totalOffers,
    Integer pendingOffers,
    Integer acceptedOffers,
    Integer declinedOffers, // Aktif redler (DECLINED)
    Integer expiredOffers,  // Pasif redler (EXPIRED)
    Integer totalRejectedOffers, // declinedOffers + expiredOffers
    BigDecimal totalValue,
    BigDecimal acceptedValue,
    BigDecimal acceptanceRate,
    BigDecimal averageOfferValue
) {} 