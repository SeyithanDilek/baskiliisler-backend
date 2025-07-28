package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.Unit;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateDto(

        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,

        @Size(max = 500, message = "Ürün açıklaması en fazla 500 karakter olabilir")
        String description,

        Unit unit,

        @Positive(message = "Birim fiyat pozitif olmalıdır")
        BigDecimal unitPrice,

        @Positive(message = "KDV oranı pozitif olmalıdır")
        BigDecimal taxRate,

        Boolean active
) {} 