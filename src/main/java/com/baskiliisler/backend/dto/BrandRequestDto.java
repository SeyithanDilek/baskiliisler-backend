package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequestDto(

        @NotBlank(message = "Marka adı boş olamaz")
        @Size(max = 100)
        String name,

        @Email(message = "Geçerli bir e-posta girin")
        String contactEmail,

        @NotBlank(message = "Telefon numarası boş olamaz")
        String contactPhone
) {}
