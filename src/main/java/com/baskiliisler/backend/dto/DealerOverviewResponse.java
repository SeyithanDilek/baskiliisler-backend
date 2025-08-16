package com.baskiliisler.backend.dto;

public record DealerOverviewResponse(
    Integer totalProducts,
    Integer totalBrands,
    Integer totalQuotes,
    Integer pendingFactoryAssignments
) {} 