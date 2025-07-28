package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FactoryRequestDto(
        @NotBlank String name,
        String address,
        String phoneNumber,
        String factoryNumber,      // Fabrika numarası (opsiyonel)
        @NotBlank @Email String userEmail,  // Fabrika kullanıcısının email'i
        Boolean active             // PATCH'te opsiyonel
) {}