package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DealerUpdateDto(
        @NotBlank(message = "Bayi adı boş olamaz")
        @Size(max = 120, message = "Bayi adı en fazla 120 karakter olabilir")
        String name,

        String address,

        @Pattern(regexp = "^\\+90\\s?5\\d{2}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}$",
                 message = "Telefon numarası +90 5XX XXX XX XX formatında olmalıdır")
        String phoneNumber,

        @Size(max = 50, message = "Vergi numarası en fazla 50 karakter olabilir")
        String taxNumber
) {
} 