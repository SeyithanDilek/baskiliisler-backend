package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.DealerCreateDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.DealerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealerService Test")
class DealerServiceTest {

    @Mock
    private DealerRepository dealerRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DealerService dealerService;

    private Dealer masterDealer;
    private Dealer testDealer;
    private DealerCreateDto testDealerCreateDto;

    @BeforeEach
    void setUp() {
        masterDealer = Dealer.builder()
                .id(1L)
                .code("MAIN")
                .name("Ana Bayi")
                .master(true)
                .build();

        testDealer = Dealer.builder()
                .id(2L)
                .code("TEST")
                .name("Test Bayi")
                .master(false)
                .build();
                
        testDealerCreateDto = DealerCreateDto.builder()
                .code("NEW")
                .name("Yeni Bayi")
                .master(false)
                .adminName("Test Admin")
                .adminEmail("admin@test.com")
                .adminPhoneNumber("+90 555 123 45 67")
                .build();
    }

    @Test
    @DisplayName("Tüm dealer'ları getirme")
    void whenGetAllDealers_thenReturnDealerList() {
        // given
        List<Dealer> dealers = Arrays.asList(masterDealer, testDealer);
        when(dealerRepository.findAll()).thenReturn(dealers);

        // when
        List<Dealer> result = dealerService.getAllDealers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(masterDealer, testDealer);
        verify(dealerRepository).findAll();
    }

    @Test
    @DisplayName("ID ile dealer getirme - başarılı")
    void whenGetDealerById_withExistingId_thenReturnDealer() {
        // given
        when(dealerRepository.findById(1L)).thenReturn(Optional.of(masterDealer));

        // when
        Optional<Dealer> result = dealerService.getDealerById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(masterDealer);
        verify(dealerRepository).findById(1L);
    }

    @Test
    @DisplayName("ID ile dealer getirme - bulunamadı")
    void whenGetDealerById_withNonExistingId_thenReturnEmpty() {
        // given
        when(dealerRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Dealer> result = dealerService.getDealerById(999L);

        // then
        assertThat(result).isEmpty();
        verify(dealerRepository).findById(999L);
    }

    @Test
    @DisplayName("Code ile dealer getirme - başarılı")
    void whenGetDealerByCode_withExistingCode_thenReturnDealer() {
        // given
        when(dealerRepository.findByCode("MAIN")).thenReturn(Optional.of(masterDealer));

        // when
        Optional<Dealer> result = dealerService.getDealerByCode("MAIN");

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(masterDealer);
        verify(dealerRepository).findByCode("MAIN");
    }

    @Test
    @DisplayName("Master dealer getirme")
    void whenGetMasterDealer_thenReturnMasterDealer() {
        // given
        when(dealerRepository.findMasterDealer()).thenReturn(Optional.of(masterDealer));

        // when
        Optional<Dealer> result = dealerService.getMasterDealer();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().isMaster()).isTrue();
        verify(dealerRepository).findMasterDealer();
    }

    @Test
    @DisplayName("Yeni dealer oluşturma - başarılı")
    void whenCreateDealer_withValidData_thenReturnSavedDealer() {
        // given
        Dealer newDealer = Dealer.builder()
                .code("NEW")
                .name("Yeni Bayi")
                .master(false)
                .build();
        
        when(dealerRepository.existsByCode("NEW")).thenReturn(false);
        when(dealerRepository.save(any(Dealer.class))).thenReturn(newDealer);
        // userService.createUser çağrısını mock'la
        when(userService.createUser(any(), any(), any())).thenReturn(null);

        // when
        Dealer result = dealerService.createDealer(testDealerCreateDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("NEW");
        assertThat(result.getName()).isEqualTo("Yeni Bayi");
        verify(dealerRepository).existsByCode("NEW");
        verify(dealerRepository).save(any(Dealer.class));
        verify(userService).createUser(any(), any(), any());
    }

    @Test
    @DisplayName("Yeni dealer oluşturma - code zaten kullanımda")
    void whenCreateDealer_withExistingCode_thenThrowException() {
        // given
        DealerCreateDto existingDealerDto = DealerCreateDto.builder()
                .code("EXISTING")
                .name("Mevcut Bayi")
                .master(false)
                .adminName("Existing Admin")
                .adminEmail("admin@existing.com")
                .adminPhoneNumber("+90 555 999 99 99")
                .build();
        
        when(dealerRepository.existsByCode("EXISTING")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> dealerService.createDealer(existingDealerDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bu dealer kodu zaten kullanılıyor");
        
        verify(dealerRepository).existsByCode("EXISTING");
        verify(dealerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dealer güncelleme - başarılı")
    void whenUpdateDealer_withValidData_thenReturnUpdatedDealer() {
        // given
        Dealer updateData = Dealer.builder()
                .code("UPDATED")
                .name("Güncellenmiş Bayi")
                .master(false)
                .build();
        
        when(dealerRepository.findById(2L)).thenReturn(Optional.of(testDealer));
        when(dealerRepository.save(any(Dealer.class))).thenReturn(testDealer);

        // when
        Dealer result = dealerService.updateDealer(2L, updateData);

        // then
        assertThat(result).isNotNull();
        verify(dealerRepository).findById(2L);
        verify(dealerRepository).save(testDealer);
    }

    @Test
    @DisplayName("Master dealer silme - hata")
    void whenDeleteMasterDealer_thenThrowException() {
        // given
        when(dealerRepository.findById(1L)).thenReturn(Optional.of(masterDealer));

        // when & then
        assertThatThrownBy(() -> dealerService.deleteDealer(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Master dealer silinemez");
        
        verify(dealerRepository).findById(1L);
        verify(dealerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Normal dealer silme - başarılı")
    void whenDeleteNormalDealer_thenSuccess() {
        // given
        when(dealerRepository.findById(2L)).thenReturn(Optional.of(testDealer));

        // when
        dealerService.deleteDealer(2L);

        // then
        verify(dealerRepository).findById(2L);
        verify(dealerRepository).delete(testDealer);
    }
} 