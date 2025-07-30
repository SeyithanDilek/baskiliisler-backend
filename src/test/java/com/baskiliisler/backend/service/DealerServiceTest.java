package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealerServiceTest {

    @Mock
    private DealerRepository dealerRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DealerService dealerService;

    private Dealer testDealer;
    private User testUser;
    private DealerRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        testDealer = Dealer.builder()
                .id(1L)
                .name("Test Dealer")
                .address("Test Address")
                .phoneNumber("+90 555 123 45 67")
                .taxNumber("1234567890")
                .active(true)
                .build();

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+90 555 123 45 67")
                .passwordHash("hashedPassword")
                .role(Role.DEALER_ADMIN)
                .dealer(testDealer)
                .build();

        testRequestDto = new DealerRequestDto(
                "Test Dealer",
                "Test Address",
                "+90 555 123 45 67",
                "1234567890",
                "Test Admin",
                "admin@test.com",
                "+90 555 987 65 43"
        );
    }

    @Test
    @DisplayName("Bayi oluşturulduğunda")
    void whenCreateDealer_thenReturnDealerResponseDto() {
        // given
        when(dealerRepository.existsByName(testRequestDto.name())).thenReturn(false);
        when(userRepository.existsByEmail(testRequestDto.adminEmail())).thenReturn(false);
        when(dealerRepository.save(any(Dealer.class))).thenReturn(testDealer);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Email gönderimi başarılı olacak şekilde mock'la
        doNothing().when(emailService).sendDealerAdminWelcomeEmail(any(), any(), any(), any());

        // when
        DealerResponseDto result = dealerService.createDealer(testRequestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(testDealer.getName());
        assertThat(result.address()).isEqualTo(testDealer.getAddress());
        assertThat(result.phoneNumber()).isEqualTo(testDealer.getPhoneNumber());
        assertThat(result.taxNumber()).isEqualTo(testDealer.getTaxNumber());
        assertThat(result.active()).isTrue();

        verify(dealerRepository).existsByName(testRequestDto.name());
        verify(userRepository).existsByEmail(testRequestDto.adminEmail());
        verify(dealerRepository).save(any(Dealer.class));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(any());
        verify(emailService).sendDealerAdminWelcomeEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Aynı isimde bayi oluşturulmaya çalışıldığında hata verir")
    void whenCreateDealerWithExistingName_thenThrowException() {
        // given
        when(dealerRepository.existsByName(testRequestDto.name())).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            dealerService.createDealer(testRequestDto);
        });

        verify(dealerRepository).existsByName(testRequestDto.name());
        verify(dealerRepository, never()).save(any(Dealer.class));
    }

    @Test
    @DisplayName("SUPER_ADMIN tüm bayileri görebilir")
    void whenGetAllDealersAsSuperAdmin_thenReturnAllDealers() {
        // given
        User superAdmin = User.builder()
                .id(1L)
                .role(Role.SUPER_ADMIN)
                .build();

        List<Dealer> dealers = List.of(testDealer);

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));
            when(dealerRepository.findByActive(true)).thenReturn(dealers);

            // when
            List<DealerResponseDto> result = dealerService.getAllDealers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo(testDealer.getName());
        }
    }

    @Test
    @DisplayName("DEALER_ADMIN sadece kendi bayisini görebilir")
    void whenGetAllDealersAsDealerAdmin_thenReturnOwnDealer() {
        // given
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // when
            List<DealerResponseDto> result = dealerService.getAllDealers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo(testDealer.getName());
        }
    }

    @Test
    @DisplayName("Bayi bulunamadığında hata verir")
    void whenFindByIdWithNonExistentDealer_thenThrowException() {
        // given
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(dealerRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThrows(EntityNotFoundException.class, () -> {
                dealerService.findById(999L);
            });
        }
    }

    @Test
    @DisplayName("Bayi güncellendiğinde")
    void whenUpdateDealer_thenReturnUpdatedDealer() {
        // given
        DealerRequestDto updateDto = new DealerRequestDto(
                "Updated Dealer",
                "Updated Address",
                "+90 555 987 65 43",
                "9876543210",
                "Updated Admin",
                "updated@test.com",
                "+90 555 111 22 33"
        );

        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(dealerRepository.findById(1L)).thenReturn(Optional.of(testDealer));
            when(dealerRepository.existsByName(updateDto.name())).thenReturn(false);
            when(dealerRepository.save(any(Dealer.class))).thenReturn(testDealer);

            // when
            DealerResponseDto result = dealerService.updateDealer(1L, updateDto);

            // then
            assertThat(result).isNotNull();
            verify(dealerRepository).save(any(Dealer.class));
        }
    }
} 