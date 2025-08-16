package com.baskiliisler.backend.dto;

import java.math.BigDecimal;

public record MostOrderedProductResponse(
    Long productId,
    String productName,
    Integer orderCount,
    BigDecimal totalRevenue
) {} 