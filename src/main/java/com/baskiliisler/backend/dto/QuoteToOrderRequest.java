package com.baskiliisler.backend.dto;

import java.time.LocalDate;
import java.util.Map;

/** ürünId  → teslim tarihi map’i */
public record QuoteToOrderRequest(
        Long quoteId,
        Map<Long, LocalDate> itemDeadlines   // productId → deadline
) {}