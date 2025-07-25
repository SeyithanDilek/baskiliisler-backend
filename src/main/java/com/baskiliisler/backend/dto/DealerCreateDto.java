package com.baskiliisler.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerCreateDto {
    
    private String code;
    private String name;
    private boolean master;
    
    // Admin kullanıcısı bilgileri
    private String adminName;
    private String adminEmail;
    private String adminPhoneNumber;
} 