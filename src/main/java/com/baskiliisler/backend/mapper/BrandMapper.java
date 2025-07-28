package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandResponseDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.type.ProcessStatus;

public class BrandMapper {

    public static Brand toEntity(BrandRequestDto dto) {
        return Brand.builder()
                .name(dto.name())
                .contactEmail(dto.contactEmail())
                .contactPhone(dto.contactPhone())
                .taxNumber(dto.taxNumber())
                .logoUrl(dto.logoUrl())
                .build();
    }

    public static BrandResponseDto toDto(Brand brand) {
        return new BrandResponseDto(
                brand.getId(),
                brand.getName(),
                brand.getContactEmail(),
                brand.getContactPhone(),
                brand.getTaxNumber(),
                brand.getLogoUrl()
        );
    }

    public static void updateEntity(BrandUpdateDto dto, Brand entity) {
        if (dto.name()          != null) entity.setName(dto.name());
        if (dto.contactEmail()  != null) entity.setContactEmail(dto.contactEmail());
        if (dto.contactPhone()  != null) entity.setContactPhone(dto.contactPhone());
        if (dto.taxNumber()     != null) entity.setTaxNumber(dto.taxNumber());
        if (dto.logoUrl()       != null) entity.setLogoUrl(dto.logoUrl());
    }
    
    public static BrandDetailDto toDetailDto(Brand brand, ProcessStatus status) {
        return new BrandDetailDto(
                brand.getId(),
                brand.getName(),
                brand.getContactEmail(),
                brand.getContactPhone(),
                brand.getTaxNumber(),
                status
        );
    }
}
