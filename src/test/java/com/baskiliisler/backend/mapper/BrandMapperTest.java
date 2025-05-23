package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandResponseDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.type.ProcessStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrandMapperTest {

    @Test
    @DisplayName("BrandRequestDto'yu Brand entity'ye dönüştürme")
    void whenToEntity_thenReturnBrandEntity() {
        // given
        BrandRequestDto dto = new BrandRequestDto(
                "Test Brand",
                "test@brand.com",
                "1234567890"
        );

        // when
        Brand result = BrandMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // Yeni entity olduğu için ID null
        assertThat(result.getName()).isEqualTo("Test Brand");
        assertThat(result.getContactEmail()).isEqualTo("test@brand.com");
        assertThat(result.getContactPhone()).isEqualTo("1234567890");
        assertThat(result.getAssignedUser()).isNull();
    }

    @Test
    @DisplayName("Brand entity'yi BrandResponseDto'ya dönüştürme")
    void whenToDto_thenReturnBrandResponseDto() {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        // when
        BrandResponseDto result = BrandMapper.toDto(brand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Brand");
        assertThat(result.contactEmail()).isEqualTo("test@brand.com");
        assertThat(result.contactPhone()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("Brand entity'yi ProcessStatus ile BrandDetailDto'ya dönüştürme")
    void whenToDetailDto_thenReturnBrandDetailDto() {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();
        ProcessStatus status = ProcessStatus.OFFER_SENT;

        // when
        BrandDetailDto result = BrandMapper.toDetailDto(brand, status);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Brand");
        assertThat(result.contactEmail()).isEqualTo("test@brand.com");
        assertThat(result.contactPhone()).isEqualTo("1234567890");
        assertThat(result.status()).isEqualTo(ProcessStatus.OFFER_SENT);
    }

    @Test
    @DisplayName("Brand entity'yi null ProcessStatus ile BrandDetailDto'ya dönüştürme")
    void whenToDetailDto_withNullStatus_thenReturnBrandDetailDtoWithNullStatus() {
        // given
        Brand brand = Brand.builder()
                .id(2L)
                .name("Another Brand")
                .contactEmail("another@brand.com")
                .contactPhone("0987654321")
                .build();

        // when
        BrandDetailDto result = BrandMapper.toDetailDto(brand, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Another Brand");
        assertThat(result.contactEmail()).isEqualTo("another@brand.com");
        assertThat(result.contactPhone()).isEqualTo("0987654321");
        assertThat(result.status()).isNull();
    }

    @Test
    @DisplayName("BrandUpdateDto ile Brand entity güncelleme - tüm alanlar")
    void whenUpdateEntity_withAllFields_thenUpdateAllFields() {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Old Name")
                .contactEmail("old@email.com")
                .contactPhone("1111111111")
                .build();

        BrandUpdateDto updateDto = new BrandUpdateDto(
                "New Name",
                "new@email.com",
                "2222222222"
        );

        // when
        BrandMapper.updateEntity(updateDto, brand);

        // then
        assertThat(brand.getId()).isEqualTo(1L); // ID değişmemeli
        assertThat(brand.getName()).isEqualTo("New Name");
        assertThat(brand.getContactEmail()).isEqualTo("new@email.com");
        assertThat(brand.getContactPhone()).isEqualTo("2222222222");
    }

    @Test
    @DisplayName("BrandUpdateDto ile Brand entity güncelleme - kısmi alanlar")
    void whenUpdateEntity_withPartialFields_thenUpdateOnlyProvidedFields() {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Original Name")
                .contactEmail("original@email.com")
                .contactPhone("1111111111")
                .build();

        BrandUpdateDto updateDto = new BrandUpdateDto(
                "Updated Name",
                null, // Email güncellenmeyecek
                "3333333333"
        );

        // when
        BrandMapper.updateEntity(updateDto, brand);

        // then
        assertThat(brand.getId()).isEqualTo(1L); // ID değişmemeli
        assertThat(brand.getName()).isEqualTo("Updated Name");
        assertThat(brand.getContactEmail()).isEqualTo("original@email.com"); // Değişmemeli
        assertThat(brand.getContactPhone()).isEqualTo("3333333333");
    }

    @Test
    @DisplayName("BrandUpdateDto ile Brand entity güncelleme - hiçbir alan")
    void whenUpdateEntity_withNullFields_thenNoFieldsUpdated() {
        // given
        Brand brand = Brand.builder()
                .id(1L)
                .name("Original Name")
                .contactEmail("original@email.com")
                .contactPhone("1111111111")
                .build();

        BrandUpdateDto updateDto = new BrandUpdateDto(null, null, null);

        // when
        BrandMapper.updateEntity(updateDto, brand);

        // then
        assertThat(brand.getId()).isEqualTo(1L);
        assertThat(brand.getName()).isEqualTo("Original Name");
        assertThat(brand.getContactEmail()).isEqualTo("original@email.com");
        assertThat(brand.getContactPhone()).isEqualTo("1111111111");
    }

    @Test
    @DisplayName("Null değerlerle BrandRequestDto'yu Brand entity'ye dönüştürme")
    void whenToEntity_withNullValues_thenReturnBrandEntityWithNullFields() {
        // given
        BrandRequestDto dto = new BrandRequestDto(
                "Test Brand",
                null, // Email null
                null  // Phone null
        );

        // when
        Brand result = BrandMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Brand");
        assertThat(result.getContactEmail()).isNull();
        assertThat(result.getContactPhone()).isNull();
    }
} 