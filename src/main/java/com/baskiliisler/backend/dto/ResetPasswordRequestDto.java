package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequestDto(
    @NotBlank(message = "Token boş olamaz")
    String token,
    
    @NotBlank(message = "Yeni şifre boş olamaz")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$",
        message = "Şifre en az 8 karakter olmalı ve en az bir küçük harf, bir büyük harf ve bir rakam içermelidir"
    )
    String newPassword
) {}
