package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.model.Dealer;

public class DealerMapper {
    
    public static DealerResponseDto toResponseDto(Dealer dealer) {
        return new DealerResponseDto(
                dealer.getId(),
                dealer.getName(),
                dealer.getAddress(),
                dealer.getPhoneNumber(),
                dealer.getTaxNumber(),
                dealer.isActive()
        );
    }
    
    public static Dealer toDealer(DealerRequestDto dto) {
        return Dealer.builder()
                .name(dto.name())
                .address(dto.address())
                .phoneNumber(dto.phoneNumber())
                .taxNumber(dto.taxNumber())
                .active(true)
                .build();
    }
    
    public static void updateDealerFromDto(Dealer dealer, DealerRequestDto dto) {
        dealer.setName(dto.name());
        dealer.setAddress(dto.address());
        dealer.setPhoneNumber(dto.phoneNumber());
        dealer.setTaxNumber(dto.taxNumber());
        // Admin bilgileri güncelleme sırasında kullanılmaz
    }
} 