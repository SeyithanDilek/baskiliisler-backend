package com.baskiliisler.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FactoryCreateDto(
        @NotNull(message = "Fabrika bilgileri boş olamaz")
        @Valid
        FactoryInfo factory,
        
        @NotNull(message = "Kullanıcı bilgileri boş olamaz")
        @Valid
        UserInfo user
) {
    
    public record FactoryInfo(
            String name,
            String address,
            String factoryNumber
    ) {}
    
    public record UserInfo(
            String name,
            String email,
            String phoneNumber
    ) {}
}
