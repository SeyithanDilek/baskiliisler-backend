package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateDto(

        @Size(max = 100, message = "Ürün kodu en fazla 100 karakter olabilir")
        String code,

        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,

        @Size(max = 20, message = "Birim en fazla 20 karakter olabilir")
        String unit,

        @Positive(message = "Birim fiyat pozitif olmalıdır")
        BigDecimal unitPrice,

        Boolean active
) {} 