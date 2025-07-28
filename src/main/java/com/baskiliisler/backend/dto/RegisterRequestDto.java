package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "İsim boş olamaz")
        @Size(max = 100, message = "İsim en fazla 100 karakter olabilir")
        String name,
        
        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 120, message = "Email en fazla 120 karakter olabilir")
        String email,
        
        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 6, max = 50, message = "Şifre 6-50 karakter arasında olmalıdır")
        String password,
        
        Role role
) {
} 