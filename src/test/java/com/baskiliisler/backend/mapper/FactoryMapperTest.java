package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.model.Factory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FactoryMapper Test")
class FactoryMapperTest {

    @Test
    @DisplayName("FactoryRequestDto'yu Factory entity'ye dönüştürme")
    void whenToEntity_thenReturnFactory() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "Test Factory",
                "Test Address, Istanbul",
                1000,
                true
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // yeni entity
        assertThat(result.getName()).isEqualTo("Test Factory");
        assertThat(result.getAddress()).isEqualTo("Test Address, Istanbul");
        assertThat(result.getDailyCapacity()).isEqualTo(1000);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Null active değeri ile FactoryRequestDto'yu entity'ye dönüştürme")
    void whenToEntity_withNullActive_thenDefaultToTrue() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "Default Factory",
                "Default Address",
                500,
                null  // null active
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Default Factory");
        assertThat(result.getAddress()).isEqualTo("Default Address");
        assertThat(result.getDailyCapacity()).isEqualTo(500);
        assertThat(result.isActive()).isTrue(); // default true
    }

    @Test
    @DisplayName("False active değeri ile FactoryRequestDto'yu entity'ye dönüştürme")
    void whenToEntity_withFalseActive_thenReturnInactive() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "Inactive Factory",
                "Inactive Address",
                200,
                false
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Inactive Factory");
        assertThat(result.getAddress()).isEqualTo("Inactive Address");
        assertThat(result.getDailyCapacity()).isEqualTo(200);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("Null dailyCapacity ile FactoryRequestDto'yu entity'ye dönüştürme")
    void whenToEntity_withNullDailyCapacity_thenReturnNull() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "No Capacity Factory",
                "No Capacity Address",
                null,  // null capacity
                true
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("No Capacity Factory");
        assertThat(result.getAddress()).isEqualTo("No Capacity Address");
        assertThat(result.getDailyCapacity()).isNull();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Factory entity'yi FactoryResponseDto'ya dönüştürme")
    void whenToDto_thenReturnFactoryResponseDto() {
        // given
        Factory factory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address, Ankara")
                .dailyCapacity(1500)
                .active(true)
                .build();

        // when
        FactoryResponseDto result = FactoryMapper.toDto(factory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Factory");
        assertThat(result.address()).isEqualTo("Test Address, Ankara");
        assertThat(result.dailyCapacity()).isEqualTo(1500);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("Inactive factory entity'yi DTO'ya dönüştürme")
    void whenToDto_withInactiveFactory_thenReturnInactiveDto() {
        // given
        Factory factory = Factory.builder()
                .id(2L)
                .name("Inactive Factory")
                .address("Inactive Address")
                .dailyCapacity(800)
                .active(false)
                .build();

        // when
        FactoryResponseDto result = FactoryMapper.toDto(factory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Inactive Factory");
        assertThat(result.address()).isEqualTo("Inactive Address");
        assertThat(result.dailyCapacity()).isEqualTo(800);
        assertThat(result.active()).isFalse();
    }

    @Test
    @DisplayName("Null dailyCapacity olan factory'yi DTO'ya dönüştürme")
    void whenToDto_withNullDailyCapacity_thenReturnDtoWithNull() {
        // given
        Factory factory = Factory.builder()
                .id(3L)
                .name("Unknown Capacity Factory")
                .address("Unknown Address")
                .dailyCapacity(null)
                .active(true)
                .build();

        // when
        FactoryResponseDto result = FactoryMapper.toDto(factory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.name()).isEqualTo("Unknown Capacity Factory");
        assertThat(result.address()).isEqualTo("Unknown Address");
        assertThat(result.dailyCapacity()).isNull();
        assertThat(result.active()).isTrue();
    }
} 