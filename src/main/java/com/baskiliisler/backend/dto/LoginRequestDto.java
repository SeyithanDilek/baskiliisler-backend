package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        String email,
        
        @NotBlank(message = "Şifre boş olamaz")
        String password
) {
} 