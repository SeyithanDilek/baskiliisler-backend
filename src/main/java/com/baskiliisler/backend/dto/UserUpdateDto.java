package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @NotBlank(message = "İsim boş olamaz")
        @Size(max = 100, message = "İsim en fazla 100 karakter olabilir")
        String name,
        
        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 120, message = "Email en fazla 120 karakter olabilir")
        String email,
        
        Role role
) {
} 