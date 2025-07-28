package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandProcessServiceTest {

    @Mock
    private BrandProcessRepository brandProcessRepository;

    @InjectMocks
    private BrandProcessService brandProcessService;

    private Brand testBrand;
    private BrandProcess testBrandProcess;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testBrandProcess = BrandProcess.builder()
                .id(1L)
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Marka Süreci Oluşturma Testleri")
    class CreateBrandProcessTests {

        @Test
        @DisplayName("Başarılı marka süreci oluşturma")
        void whenCreateBrandProcess_thenReturnCreatedProcess() {
            // given
            when(brandProcessRepository.save(any(BrandProcess.class))).thenReturn(testBrandProcess);

            // when
            BrandProcess result = brandProcessService.createBrandProcess(testBrand);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBrand()).isEqualTo(testBrand);
            assertThat(result.getStatus()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
            assertThat(result.getUpdatedAt()).isNotNull();

            verify(brandProcessRepository).save(any(BrandProcess.class));
        }
    }

    @Nested
    @DisplayName("Marka Süreci Durumu Güncelleme Testleri")
    class UpdateBrandProcessStatusTests {

        @Test
        @DisplayName("Başarılı durum güncelleme")
        void whenUpdateBrandProcessStatus_thenReturnUpdatedProcess() {
            // given
            Long brandId = 1L;
            ProcessStatus newStatus = ProcessStatus.OFFER_SENT;
            
            when(brandProcessRepository.findByBrandIdForUpdate(brandId))
                    .thenReturn(Optional.of(testBrandProcess));

            // when
            BrandProcess result = brandProcessService.updateBrandProcessStatus(brandId, newStatus);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(newStatus);
            assertThat(result.getUpdatedAt()).isNotNull();

            verify(brandProcessRepository).findByBrandIdForUpdate(brandId);
        }

        @Test
        @DisplayName("Olmayan marka için durum güncelleme")
        void whenUpdateBrandProcessStatus_withNonExistingBrand_thenThrowException() {
            // given
            Long nonExistingBrandId = 999L;
            ProcessStatus newStatus = ProcessStatus.OFFER_SENT;
            
            when(brandProcessRepository.findByBrandIdForUpdate(nonExistingBrandId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> brandProcessService.updateBrandProcessStatus(nonExistingBrandId, newStatus))
                    .isInstanceOf(RuntimeException.class);

            verify(brandProcessRepository).findByBrandIdForUpdate(nonExistingBrandId);
        }
    }

    @Nested
    @DisplayName("Marka Süreci Getirme Testleri")
    class GetBrandProcessTests {

        @Test
        @DisplayName("Başarılı marka süreci getirme")
        void whenGetBrandProcess_thenReturnProcess() {
            // given
            Long brandId = 1L;
            when(brandProcessRepository.findByBrandId(brandId))
                    .thenReturn(Optional.of(testBrandProcess));

            // when
            BrandProcess result = brandProcessService.getBrandProcess(brandId);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testBrandProcess);

            verify(brandProcessRepository).findByBrandId(brandId);
        }

        @Test
        @DisplayName("Olmayan marka süreci getirme")
        void whenGetBrandProcess_withNonExistingBrand_thenThrowException() {
            // given
            Long nonExistingBrandId = 999L;
            when(brandProcessRepository.findByBrandId(nonExistingBrandId))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> brandProcessService.getBrandProcess(nonExistingBrandId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Brand process not found");

            verify(brandProcessRepository).findByBrandId(nonExistingBrandId);
        }
    }

    @Nested
    @DisplayName("Süreç Durumu Getirme Testleri")
    class GetProcessStatusTests {

        @Test
        @DisplayName("Başarılı durum getirme")
        void whenGetProcessStatus_thenReturnStatus() {
            // given
            Long brandId = 1L;
            when(brandProcessRepository.findByBrandId(brandId))
                    .thenReturn(Optional.of(testBrandProcess));

            // when
            ProcessStatus result = brandProcessService.getProcessStatus(brandId);

            // then
            assertThat(result).isEqualTo(ProcessStatus.SAMPLE_LEFT);

            verify(brandProcessRepository).findByBrandId(brandId);
        }

        @Test
        @DisplayName("Olmayan marka için durum getirme")
        void whenGetProcessStatus_withNonExistingBrand_thenReturnNull() {
            // given
            Long nonExistingBrandId = 999L;
            when(brandProcessRepository.findByBrandId(nonExistingBrandId))
                    .thenReturn(Optional.empty());

            // when
            ProcessStatus result = brandProcessService.getProcessStatus(nonExistingBrandId);

            // then
            assertThat(result).isNull();

            verify(brandProcessRepository).findByBrandId(nonExistingBrandId);
        }
    }

    @Nested
    @DisplayName("Marka Süreci Varlık Kontrolü Testleri")
    class ExistsBrandProcessTests {

        @Test
        @DisplayName("Mevcut marka süreci kontrolü")
        void whenExistsBrandProcess_withExistingBrand_thenReturnTrue() {
            // given
            Long brandId = 1L;
            when(brandProcessRepository.existsByBrandId(brandId)).thenReturn(true);

            // when
            boolean result = brandProcessService.existsBrandProcess(brandId);

            // then
            assertThat(result).isTrue();

            verify(brandProcessRepository).existsByBrandId(brandId);
        }

        @Test
        @DisplayName("Olmayan marka süreci kontrolü")
        void whenExistsBrandProcess_withNonExistingBrand_thenReturnFalse() {
            // given
            Long nonExistingBrandId = 999L;
            when(brandProcessRepository.existsByBrandId(nonExistingBrandId)).thenReturn(false);

            // when
            boolean result = brandProcessService.existsBrandProcess(nonExistingBrandId);

            // then
            assertThat(result).isFalse();

            verify(brandProcessRepository).existsByBrandId(nonExistingBrandId);
        }
    }
} 