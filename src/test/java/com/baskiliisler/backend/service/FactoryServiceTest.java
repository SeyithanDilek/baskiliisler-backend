package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.model.Factory;
import com.baskiliisler.backend.repository.FactoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("FactoryService Test")
class FactoryServiceTest {

    @Mock
    private FactoryRepository factoryRepository;

    @InjectMocks
    private FactoryService factoryService;

    private Factory testFactory;

    @BeforeEach
    void setUp() {
        testFactory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address, Istanbul")
                .dailyCapacity(1000)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Var olan fabrika ID'si ile fabrika getirme")
    void whenGetFactoryById_withExistingId_thenReturnFactory() {
        // given
        Long factoryId = 1L;
        when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(testFactory));

        // when
        Factory result = factoryService.getFactoryById(factoryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Factory");
        assertThat(result.getAddress()).isEqualTo("Test Address, Istanbul");
        assertThat(result.getDailyCapacity()).isEqualTo(1000);
        assertThat(result.isActive()).isTrue();

        verify(factoryRepository).findById(factoryId);
    }

    @Test
    @DisplayName("Olmayan fabrika ID'si ile fabrika getirme")
    void whenGetFactoryById_withNonExistingId_thenThrowException() {
        // given
        Long nonExistingId = 999L;
        when(factoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factoryService.getFactoryById(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factory not found");

        verify(factoryRepository).findById(nonExistingId);
    }

    @Test
    @DisplayName("Null ID ile fabrika getirme")
    void whenGetFactoryById_withNullId_thenThrowException() {
        // given
        when(factoryRepository.findById(null)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factoryService.getFactoryById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factory not found");

        verify(factoryRepository).findById(null);
    }

    @Test
    @DisplayName("Yeni fabrika oluşturma")
    void whenCreate_thenReturnSavedFactory() {
        // given
        FactoryRequestDto dto = new FactoryRequestDto(
                "New Factory",
                "New Address",
                1200,
                true
        );
        
        Factory savedFactory = Factory.builder()
                .id(1L)
                .name("New Factory")
                .address("New Address")
                .dailyCapacity(1200)
                .active(true)
                .build();
        
        when(factoryRepository.save(any(Factory.class))).thenReturn(savedFactory);

        // when
        Factory result = factoryService.create(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New Factory");
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.getDailyCapacity()).isEqualTo(1200);
        assertThat(result.isActive()).isTrue();

        ArgumentCaptor<Factory> factoryCaptor = ArgumentCaptor.forClass(Factory.class);
        verify(factoryRepository).save(factoryCaptor.capture());
        
        Factory capturedFactory = factoryCaptor.getValue();
        assertThat(capturedFactory.getName()).isEqualTo("New Factory");
        assertThat(capturedFactory.getAddress()).isEqualTo("New Address");
        assertThat(capturedFactory.getDailyCapacity()).isEqualTo(1200);
        assertThat(capturedFactory.isActive()).isTrue();
    }

    @Test
    @DisplayName("Fabrika güncelleme - tüm alanlar")
    void whenUpdate_withAllFields_thenReturnUpdatedFactory() {
        // given
        Long factoryId = 1L;
        FactoryRequestDto dto = new FactoryRequestDto(
                "Updated Factory",
                "Updated Address",
                2000,
                false
        );
        
        when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(testFactory));

        // when
        Factory result = factoryService.update(factoryId, dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Factory");
        assertThat(result.getAddress()).isEqualTo("Updated Address");
        assertThat(result.getDailyCapacity()).isEqualTo(2000);
        assertThat(result.isActive()).isFalse();

        verify(factoryRepository).findById(factoryId);
    }

    @Test
    @DisplayName("Fabrika güncelleme - kısmi alanlar")
    void whenUpdate_withPartialFields_thenReturnPartiallyUpdatedFactory() {
        // given
        Long factoryId = 1L;
        FactoryRequestDto dto = new FactoryRequestDto(
                "Partially Updated Factory",
                null,  // address güncellenmeyecek
                null,  // dailyCapacity güncellenmeyecek
                null   // active güncellenmeyecek
        );
        
        when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(testFactory));

        // when
        Factory result = factoryService.update(factoryId, dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Partially Updated Factory");
        assertThat(result.getAddress()).isEqualTo("Test Address, Istanbul"); // eski değer
        assertThat(result.getDailyCapacity()).isEqualTo(1000); // eski değer
        assertThat(result.isActive()).isTrue(); // eski değer

        verify(factoryRepository).findById(factoryId);
    }

    @Test
    @DisplayName("Olmayan fabrikayı güncelleme")
    void whenUpdate_withNonExistingId_thenThrowException() {
        // given
        Long nonExistingId = 999L;
        FactoryRequestDto dto = new FactoryRequestDto(
                "Non Existing Factory",
                "Address",
                500,
                true
        );
        
        when(factoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factoryService.update(nonExistingId, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factory not found");

        verify(factoryRepository).findById(nonExistingId);
    }

    @Test
    @DisplayName("Tüm fabrikaları listeleme")
    void whenList_withOnlyActiveFalse_thenReturnAllFactories() {
        // given
        Factory activeFactory = Factory.builder()
                .id(1L)
                .name("Active Factory")
                .address("Active Address")
                .dailyCapacity(800)
                .active(true)
                .build();

        Factory inactiveFactory = Factory.builder()
                .id(2L)
                .name("Inactive Factory")
                .address("Inactive Address")
                .dailyCapacity(600)
                .active(false)
                .build();

        when(factoryRepository.findAll()).thenReturn(List.of(activeFactory, inactiveFactory));

        // when
        List<FactoryResponseDto> result = factoryService.list(false);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FactoryResponseDto::name)
                .containsExactlyInAnyOrder("Active Factory", "Inactive Factory");
        assertThat(result).extracting(FactoryResponseDto::active)
                .containsExactlyInAnyOrder(true, false);

        verify(factoryRepository).findAll();
        verify(factoryRepository, never()).findByActiveTrue();
    }

    @Test
    @DisplayName("Sadece aktif fabrikaları listeleme")
    void whenList_withOnlyActiveTrue_thenReturnOnlyActiveFactories() {
        // given
        Factory activeFactory = Factory.builder()
                .id(1L)
                .name("Active Factory")
                .address("Active Address")
                .dailyCapacity(800)
                .active(true)
                .build();

        when(factoryRepository.findByActiveTrue()).thenReturn(List.of(activeFactory));

        // when
        List<FactoryResponseDto> result = factoryService.list(true);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Active Factory");
        assertThat(result.get(0).active()).isTrue();

        verify(factoryRepository).findByActiveTrue();
        verify(factoryRepository, never()).findAll();
    }

    @Test
    @DisplayName("Fabrika ID ile DTO getirme")
    void whenGet_withExistingId_thenReturnFactoryResponseDto() {
        // given
        Long factoryId = 1L;
        when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(testFactory));

        // when
        FactoryResponseDto result = factoryService.get(factoryId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Factory");
        assertThat(result.address()).isEqualTo("Test Address, Istanbul");
        assertThat(result.dailyCapacity()).isEqualTo(1000);
        assertThat(result.active()).isTrue();

        verify(factoryRepository).findById(factoryId);
    }

    @Test
    @DisplayName("Olmayan fabrika ID ile DTO getirme")
    void whenGet_withNonExistingId_thenThrowException() {
        // given
        Long nonExistingId = 999L;
        when(factoryRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> factoryService.get(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factory not found");

        verify(factoryRepository).findById(nonExistingId);
    }

    @Test
    @DisplayName("Fabrika silme")
    void whenDelete_thenCallRepositoryDelete() {
        // given
        Long factoryId = 1L;

        // when
        factoryService.delete(factoryId);

        // then
        verify(factoryRepository).deleteById(factoryId);
    }
} 