package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record BrandUpdateDto(
        @Size(max = 100)  String name,
        @Email String contactEmail,
        String contactPhone
) {}