package com.baskiliisler.backend.dto;

import java.time.LocalDateTime;

public record BrandResponseDto(
        Long id,
        String name,
        String contactEmail,
        String contactPhone,
        String taxNumber,
        String logoUrl,
        LocalDateTime createdAt,
        Long assignedUserId
) {}
