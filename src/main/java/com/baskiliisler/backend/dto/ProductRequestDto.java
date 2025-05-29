package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequestDto(

        @NotBlank(message = "Ürün kodu boş olamaz")
        @Size(max = 100, message = "Ürün kodu en fazla 100 karakter olabilir")
        String code,

        @NotBlank(message = "Ürün adı boş olamaz")
        @Size(max = 200, message = "Ürün adı en fazla 200 karakter olabilir")
        String name,

        @NotBlank(message = "Birim boş olamaz")
        @Size(max = 20, message = "Birim en fazla 20 karakter olabilir")
        String unit,

        @NotNull(message = "Birim fiyat boş olamaz")
        @Positive(message = "Birim fiyat pozitif olmalıdır")
        BigDecimal unitPrice
) {} 