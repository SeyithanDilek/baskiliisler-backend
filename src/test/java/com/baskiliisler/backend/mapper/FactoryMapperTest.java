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
                "+90 555 123 45 67",
                true
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // yeni entity
        assertThat(result.getName()).isEqualTo("Test Factory");
        assertThat(result.getAddress()).isEqualTo("Test Address, Istanbul");
        assertThat(result.getPhoneNumber()).isEqualTo("+90 555 123 45 67");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Null active değeri ile FactoryRequestDto'yu entity'ye dönüştürme")
    void whenToEntity_withNullActive_thenDefaultToTrue() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "Default Factory",
                "Default Address",
                "+90 555 111 22 33",
                null  // null active
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Default Factory");
        assertThat(result.getAddress()).isEqualTo("Default Address");
        assertThat(result.getPhoneNumber()).isEqualTo("+90 555 111 22 33");
        assertThat(result.isActive()).isTrue(); // default true
    }

    @Test
    @DisplayName("False active değeri ile FactoryRequestDto'yu entity'ye dönüştürme")
    void whenToEntity_withFalseActive_thenReturnInactive() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "Inactive Factory",
                "Inactive Address",
                "+90 555 999 88 77",
                false
        );

        // when
        Factory result = FactoryMapper.toEntity(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Inactive Factory");
        assertThat(result.getAddress()).isEqualTo("Inactive Address");
        assertThat(result.getPhoneNumber()).isEqualTo("+90 555 999 88 77");
        assertThat(result.isActive()).isFalse();
    }

    @Test
    @DisplayName("Factory entity'yi FactoryResponseDto'ya dönüştürme")
    void whenToDto_thenReturnFactoryResponseDto() {
        // given
        Factory factory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address, Ankara")
                .phoneNumber("+90 555 444 33 22")
                .active(true)
                .build();

        // when
        FactoryResponseDto result = FactoryMapper.toDto(factory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Factory");
        assertThat(result.address()).isEqualTo("Test Address, Ankara");
        assertThat(result.phoneNumber()).isEqualTo("+90 555 444 33 22");
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
                .phoneNumber("+90 555 777 66 55")
                .active(false)
                .build();

        // when
        FactoryResponseDto result = FactoryMapper.toDto(factory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.name()).isEqualTo("Inactive Factory");
        assertThat(result.address()).isEqualTo("Inactive Address");
        assertThat(result.phoneNumber()).isEqualTo("+90 555 777 66 55");
        assertThat(result.active()).isFalse();
    }
} 