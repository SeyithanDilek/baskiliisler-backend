package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.model.Factory;

public class FactoryMapper {
    public static Factory toEntity(FactoryRequestDto d) {
        return Factory.builder()
                .name(d.name())
                .address(d.address())
                .dailyCapacity(d.dailyCapacity())
                .active(d.active() == null || d.active())   // default true
                .build();
    }
    public static FactoryResponseDto toDto(Factory f) {
        return new FactoryResponseDto(
                f.getId(), f.getName(), f.getAddress(),
                f.getDailyCapacity(), f.isActive());
    }
}
