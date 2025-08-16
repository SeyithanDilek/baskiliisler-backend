package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.DealerAdminInfo;
import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerUpdateDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.common.Role;

import java.util.List;

public class DealerMapper {
    
    public static DealerResponseDto toResponseDto(Dealer dealer, UserRepository userRepository) {
        DealerAdminInfo adminInfo = null;
        
        // Dealer'ın admin kullanıcısını bul
        List<User> dealerUsers = userRepository.findByDealer(dealer);
        User adminUser = dealerUsers.stream()
                .filter(user -> user.getRole() == Role.DEALER_ADMIN)
                .findFirst()
                .orElse(null);
        
        if (adminUser != null) {
            adminInfo = new DealerAdminInfo(
                    adminUser.getId(),
                    adminUser.getName(),
                    adminUser.getEmail(),
                    adminUser.getPhoneNumber()
            );
        }
        
        return new DealerResponseDto(
                dealer.getId(),
                dealer.getName(),
                dealer.getAddress(),
                dealer.getPhoneNumber(),
                dealer.getTaxNumber(),
                dealer.isActive(),
                adminInfo
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
    
    public static void updateDealerFromUpdateDto(Dealer dealer, DealerUpdateDto dto) {
        dealer.setName(dto.name());
        dealer.setAddress(dto.address());
        dealer.setPhoneNumber(dto.phoneNumber());
        dealer.setTaxNumber(dto.taxNumber());
    }
    

} 