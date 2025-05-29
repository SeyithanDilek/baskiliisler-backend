package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Mevcut şifre boş olamaz")
        String currentPassword,
        
        @NotBlank(message = "Yeni şifre boş olamaz")
        @Size(min = 6, max = 50, message = "Yeni şifre 6-50 karakter arasında olmalıdır")
        String newPassword
) {
} 