package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteItemRequestDto;
import com.baskiliisler.backend.dto.QuoteUpdateDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.QuoteItem;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.repository.QuoteRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private QuoteItemService quoteItemService;

    @Mock
    private BrandProcessService brandProcessService;

    @Mock
    private BrandProcessHistoryService brandProcessHistoryService;

    @InjectMocks
    private QuoteService quoteService;

    private Brand testBrand;
    private Quote testQuote;
    private QuoteCreateDto testCreateDto;
    private QuoteUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(new ArrayList<>())
                .build();

        QuoteItemRequestDto item = new QuoteItemRequestDto(
                1L,
                2,
                BigDecimal.valueOf(1000)
        );

        testCreateDto = new QuoteCreateDto(
                1L,
                List.of(item),
                LocalDate.now().plusDays(30)
        );

        testUpdateDto = new QuoteUpdateDto(
                List.of(item),
                LocalDate.now().plusDays(45)
        );
    }

    @Nested
    @DisplayName("Teklif Oluşturma Testleri")
    class CreateQuoteTests {

        @Test
        @DisplayName("Başarılı teklif oluşturma")
        void whenCreateQuote_thenReturnCreatedQuote() {
            // given
            BrandProcess brandProcess = BrandProcess.builder()
                    .brand(testBrand)
                    .status(ProcessStatus.OFFER_SENT)
                    .build();

            when(brandRepository.findById(testCreateDto.brandId())).thenReturn(Optional.of(testBrand));
            when(quoteRepository.save(any(Quote.class))).thenReturn(testQuote);
            when(quoteItemService.assembleAndSaveQuoteItems(any(Quote.class), any(List.class)))
                    .thenReturn(BigDecimal.valueOf(2000));
            when(brandProcessService.updateBrandProcessStatus(any(Long.class), any(ProcessStatus.class)))
                    .thenReturn(brandProcess);
            doNothing().when(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    any(BrandProcess.class), any(ProcessStatus.class), any(ProcessStatus.class), anyString());

            // when
            Quote result = quoteService.createQuote(testCreateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getBrand()).isEqualTo(testBrand);
            assertThat(result.getStatus()).isEqualTo(QuoteStatus.OFFER_SENT);
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(2000));
            assertThat(result.getCurrency()).isEqualTo("TRY");

            verify(brandRepository).findById(testCreateDto.brandId());
            verify(quoteRepository).save(any(Quote.class));
            verify(quoteItemService).assembleAndSaveQuoteItems(any(Quote.class), eq(testCreateDto.items()));
            verify(brandProcessService).updateBrandProcessStatus(testBrand.getId(), ProcessStatus.OFFER_SENT);
            verify(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    any(BrandProcess.class), eq(ProcessStatus.OFFER_SENT), eq(ProcessStatus.SAMPLE_LEFT), anyString());
        }

        @Test
        @DisplayName("Olmayan marka ile teklif oluşturma")
        void whenCreateQuote_withNonExistingBrand_thenThrowException() {
            // given
            when(brandRepository.findById(testCreateDto.brandId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteService.createQuote(testCreateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Brand not found");

            verify(brandRepository).findById(testCreateDto.brandId());
            verify(quoteRepository, never()).save(any());
            verify(quoteItemService, never()).assembleAndSaveQuoteItems(any(), any());
        }
    }

    @Nested
    @DisplayName("Teklif Güncelleme Testleri")
    class UpdateQuoteTests {

        @Test
        @DisplayName("Başarılı teklif güncelleme")
        void whenUpdateQuote_thenReturnUpdatedQuote() {
            // given
            Long quoteId = 1L;
            BrandProcess brandProcess = BrandProcess.builder()
                    .brand(testBrand)
                    .status(ProcessStatus.OFFER_SENT)
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(testQuote));
            when(quoteItemService.assembleAndSaveQuoteItems(any(Quote.class), any(List.class)))
                    .thenReturn(BigDecimal.valueOf(3000));
            when(brandProcessService.updateBrandProcessStatus(any(Long.class), any(ProcessStatus.class)))
                    .thenReturn(brandProcess);
            doNothing().when(quoteItemService).deleteQuoteItems(any(List.class));
            doNothing().when(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    any(BrandProcess.class), any(ProcessStatus.class), any(ProcessStatus.class), anyString());

            // when
            Quote result = quoteService.updateQuote(quoteId, testUpdateDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getValidUntil()).isEqualTo(testUpdateDto.validUntil());
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(3000));
            assertThat(result.getStatus()).isEqualTo(QuoteStatus.OFFER_SENT);
            assertThat(result.getUpdatedAt()).isNotNull();

            verify(quoteRepository).findById(quoteId);
            verify(quoteItemService).deleteQuoteItems(testQuote.getItems());
            verify(quoteItemService).assembleAndSaveQuoteItems(testQuote, testUpdateDto.items());
            verify(brandProcessService).updateBrandProcessStatus(testBrand.getId(), ProcessStatus.OFFER_SENT);
        }

        @Test
        @DisplayName("Olmayan teklifi güncelleme")
        void whenUpdateQuote_withNonExistingQuote_thenThrowException() {
            // given
            Long nonExistingId = 999L;
            when(quoteRepository.findById(nonExistingId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteService.updateQuote(nonExistingId, testUpdateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Teklif bulunamadı");

            verify(quoteRepository).findById(nonExistingId);
            verify(quoteItemService, never()).deleteQuoteItems(any());
            verify(quoteItemService, never()).assembleAndSaveQuoteItems(any(), any());
        }

        @Test
        @DisplayName("Güncellenemez durumda olan teklifi güncelleme")
        void whenUpdateQuote_withNonUpdatableStatus_thenThrowException() {
            // given
            Long quoteId = 1L;
            Quote completedQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.ACCEPTED)
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(completedQuote));

            // when & then
            assertThatThrownBy(() -> quoteService.updateQuote(quoteId, testUpdateDto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Bu teklif güncellenemez");

            verify(quoteRepository).findById(quoteId);
            verify(quoteItemService, never()).deleteQuoteItems(any());
            verify(quoteItemService, never()).assembleAndSaveQuoteItems(any(), any());
        }
    }
} 