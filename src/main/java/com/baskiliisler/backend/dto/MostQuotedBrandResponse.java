package com.baskiliisler.backend.dto;

import java.math.BigDecimal;

public record MostQuotedBrandResponse(
    Long brandId,
    String brandName,
    Integer quoteCount,
    BigDecimal totalRevenue
) {} 