package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DealerMapperTest {

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toResponseDto_ShouldMapDealerToResponseDto() {
        // Given
        Dealer dealer = Dealer.builder()
                .id(1L)
                .name("Test Dealer")
                .address("Test Address")
                .phoneNumber("+90 555 123 45 67")
                .taxNumber("1234567890")
                .active(true)
                .build();

        // Mock empty user list (no admin)
        when(userRepository.findByDealer(dealer)).thenReturn(List.of());

        // When
        DealerResponseDto result = DealerMapper.toResponseDto(dealer, userRepository);

        // Then
        assertEquals(dealer.getId(), result.id());
        assertEquals(dealer.getName(), result.name());
        assertEquals(dealer.getAddress(), result.address());
        assertEquals(dealer.getPhoneNumber(), result.phoneNumber());
        assertEquals(dealer.getTaxNumber(), result.taxNumber());
        assertEquals(dealer.isActive(), result.active());
        assertNull(result.admin()); // No admin found
    }

    @Test
    void toDealer_ShouldMapRequestDtoToDealer() {
        // Given
        DealerRequestDto dto = new DealerRequestDto(
                "Test Dealer",
                "Test Address",
                "+90 555 123 45 67",
                "1234567890",
                "Test Admin",
                "admin@test.com",
                "+90 555 987 65 43"
        );

        // When
        Dealer result = DealerMapper.toDealer(dto);

        // Then
        assertEquals(dto.name(), result.getName());
        assertEquals(dto.address(), result.getAddress());
        assertEquals(dto.phoneNumber(), result.getPhoneNumber());
        assertEquals(dto.taxNumber(), result.getTaxNumber());
        assertTrue(result.isActive());
        assertNull(result.getId()); // ID henüz set edilmemiş
    }

    @Test
    void updateDealerFromDto_ShouldUpdateDealerFields() {
        // Given
        Dealer dealer = Dealer.builder()
                .id(1L)
                .name("Old Name")
                .address("Old Address")
                .phoneNumber("+90 555 000 00 00")
                .taxNumber("0000000000")
                .active(true)
                .build();

        DealerRequestDto dto = new DealerRequestDto(
                "New Name",
                "New Address",
                "+90 555 123 45 67",
                "1234567890",
                "New Admin",
                "newadmin@test.com",
                "+90 555 111 22 33"
        );

        // When
        DealerMapper.updateDealerFromDto(dealer, dto);

        // Then
        assertEquals(dto.name(), dealer.getName());
        assertEquals(dto.address(), dealer.getAddress());
        assertEquals(dto.phoneNumber(), dealer.getPhoneNumber());
        assertEquals(dto.taxNumber(), dealer.getTaxNumber());
        // ID ve active değişmemeli
        assertEquals(1L, dealer.getId());
        assertTrue(dealer.isActive());
    }
} 