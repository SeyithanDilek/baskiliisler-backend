package com.baskiliisler.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record BrandUpdateDto(
        @Size(max = 100)  String name,
        @Email String contactEmail,
        String contactPhone,
        String taxNumber,                           // vergi numarası (opsiyonel)
        String logoUrl,                             // marka logosu URL'i (opsiyonel)
        Long assignedUserId                         // atanmış kullanıcı ID'si (opsiyonel)
) {}