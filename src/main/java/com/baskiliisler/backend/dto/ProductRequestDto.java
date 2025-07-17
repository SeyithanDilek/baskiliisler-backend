package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.Unit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDto(

        @NotBlank(message = "Ürün adı boş olamaz")
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,

        @Size(max = 500, message = "Ürün açıklaması en fazla 500 karakter olabilir")
        String description,

        @NotNull(message = "Birim boş olamaz")
        Unit unit,

        @NotNull(message = "Birim fiyat boş olamaz")
        @Positive(message = "Birim fiyat pozitif olmalıdır")
        BigDecimal unitPrice,

        @NotNull(message = "KDV oranı boş olamaz")
        @Positive(message = "KDV oranı pozitif olmalıdır")
        BigDecimal taxRate
) {} 