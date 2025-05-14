package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.repository.BrandProcessRepository;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandProcessRepository processRepository;

    @InjectMocks
    private BrandService brandService;

    private BrandRequestDto testBrandRequest;
    private Brand testBrand;

    @BeforeEach
    void setUp() {
        testBrandRequest = new BrandRequestDto(
                "Test Brand",
                "contact@test.com",
                "1234567890"
        );

        testBrand = Brand.builder()
                .id(1L)
                .name(testBrandRequest.name())
                .contactEmail(testBrandRequest.contactEmail())
                .contactPhone(testBrandRequest.contactPhone())
                .build();
    }

    @Test
    @DisplayName("Yeni marka oluşturulduğunda")
    void whenCreateBrand_thenSaveBrandAndProcess() {
        // given
        when(brandRepository.findByName(testBrandRequest.name())).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(testBrand);
        when(processRepository.save(any(BrandProcess.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Brand savedBrand = brandService.createBrand(testBrandRequest);

        // then
        assertThat(savedBrand).isNotNull();
        assertThat(savedBrand.getName()).isEqualTo(testBrandRequest.name());
        
        ArgumentCaptor<BrandProcess> processCaptor = ArgumentCaptor.forClass(BrandProcess.class);
        verify(processRepository).save(processCaptor.capture());
        
        BrandProcess savedProcess = processCaptor.getValue();
        assertThat(savedProcess.getBrand()).isEqualTo(testBrand);
        assertThat(savedProcess.getStatus()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
    }

    @Test
    @DisplayName("Var olan marka adı ile oluşturulmaya çalışıldığında")
    void whenCreateBrand_withExistingName_thenThrowException() {
        // given
        when(brandRepository.findByName(testBrandRequest.name())).thenReturn(Optional.of(testBrand));

        // when & then
        assertThatThrownBy(() -> brandService.createBrand(testBrandRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bu marka zaten mevcut");
        
        verify(brandRepository, never()).save(any());
        verify(processRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tüm markalar listelendiğinde")
    void whenGetAllBrands_thenReturnList() {
        // given
        List<Brand> brands = List.of(testBrand);
        when(brandRepository.findAll()).thenReturn(brands);

        // when
        List<Brand> result = brandService.getAllBrands();

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .contains(testBrand);
    }

    @Test
    @DisplayName("ID ile marka detayı bulunduğunda")
    void whenFindById_thenReturnBrandDetail() {
        // given
        Long brandId = 1L;
        BrandProcess process = BrandProcess.builder()
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .build();

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(processRepository.findByBrandId(brandId)).thenReturn(Optional.of(process));

        // when
        BrandDetailDto result = brandService.findById(brandId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(testBrand.getId());
        assertThat(result.name()).isEqualTo(testBrand.getName());
        assertThat(result.status()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
    }

    @Test
    @DisplayName("Olmayan ID ile marka detayı arandığında")
    void whenFindById_withNonExistingId_thenThrowException() {
        // given
        Long nonExistingId = 999L;
        when(brandRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> brandService.findById(nonExistingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Brand not found");
    }

    @Test
    @DisplayName("Marka güncellendiğinde")
    void whenUpdateBrand_thenReturnUpdatedBrandDetail() {
        // given
        Long brandId = 1L;
        BrandUpdateDto updateDto = new BrandUpdateDto(
                "Updated Brand",
                "updated@test.com",
                "9876543210"
        );
        BrandProcess process = BrandProcess.builder()
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .build();

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.findByName(updateDto.name())).thenReturn(Optional.empty());
        when(processRepository.findByBrandId(brandId)).thenReturn(Optional.of(process));

        // when
        BrandDetailDto result = brandService.updateBrand(brandId, updateDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(updateDto.name());
        assertThat(result.contactEmail()).isEqualTo(updateDto.contactEmail());
        assertThat(result.contactPhone()).isEqualTo(updateDto.contactPhone());
        assertThat(result.status()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
    }

    @Test
    @DisplayName("Var olan isimle marka güncellenmeye çalışıldığında")
    void whenUpdateBrand_withExistingName_thenThrowException() {
        // given
        Long brandId = 1L;
        String existingName = "Existing Brand";
        BrandUpdateDto updateDto = new BrandUpdateDto(existingName, null, null);
        Brand existingBrand = Brand.builder().name(existingName).build();

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
        when(brandRepository.findByName(existingName)).thenReturn(Optional.of(existingBrand));

        // when & then
        assertThatThrownBy(() -> brandService.updateBrand(brandId, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bu isim zaten kullanımda");
    }

    @Test
    @DisplayName("Marka silindiğinde")
    void whenDeleteBrand_thenDeleteSuccessfully() {
        // given
        Long brandId = 1L;
        when(processRepository.existsByBrandId(brandId)).thenReturn(false);

        // when
        brandService.deleteBrand(brandId);

        // then
        verify(brandRepository).deleteById(brandId);
    }

    @Test
    @DisplayName("Aktif süreci olan marka silinmeye çalışıldığında")
    void whenDeleteBrand_withActiveProcess_thenThrowException() {
        // given
        Long brandId = 1L;
        when(processRepository.existsByBrandId(brandId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> brandService.deleteBrand(brandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Süreç devam ediyor, marka silinemez");

        verify(brandRepository, never()).deleteById(any());
    }
} 