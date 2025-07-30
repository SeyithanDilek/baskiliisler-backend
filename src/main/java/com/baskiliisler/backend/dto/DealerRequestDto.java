package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DealerRequestDto(
        @NotBlank(message = "Bayi adı boş olamaz")
        @Size(max = 120, message = "Bayi adı en fazla 120 karakter olabilir")
        String name,
        
        String address,
        
        @Pattern(regexp = "^\\+90\\s?5\\d{2}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}$", 
                 message = "Telefon numarası +90 5XX XXX XX XX formatında olmalıdır")
        String phoneNumber,
        
        @Size(max = 50, message = "Vergi numarası en fazla 50 karakter olabilir")
        String taxNumber,
        
        // Dealer Admin bilgileri
        @NotBlank(message = "Admin adı boş olamaz")
        @Size(max = 100, message = "Admin adı en fazla 100 karakter olabilir")
        String adminName,
        
        @NotBlank(message = "Admin email boş olamaz")
        @Email(message = "Geçerli bir email adresi giriniz")
        @Size(max = 120, message = "Email en fazla 120 karakter olabilir")
        String adminEmail,
        
        @NotBlank(message = "Admin telefon numarası boş olamaz")
        @Pattern(regexp = "^\\+90\\s?5\\d{2}\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}$", 
                 message = "Telefon numarası +90 5XX XXX XX XX formatında olmalıdır")
        String adminPhoneNumber
) {
} 