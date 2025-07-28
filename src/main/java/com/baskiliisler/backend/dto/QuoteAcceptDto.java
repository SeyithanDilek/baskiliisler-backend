package com.baskiliisler.backend.dto;

import java.time.LocalDate;
import java.util.Map;

public record QuoteAcceptDto(
        Map<Long, LocalDate> itemDeadlines,
        String customerLogoUrl,                       // müşterinin logosu
        String description,                           // sipariş açıklaması
        String customerTaxNumber                      // müşterinin vergi numarası
) {}
