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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Mock
    private EntityManager entityManager;

    @Mock
    private com.baskiliisler.backend.notification.service.NotificationService notificationService;

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
            verify(quoteRepository, times(2)).save(any(Quote.class)); // İlk save ve totalPrice update sonrası ikinci save
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

    @Nested
    @DisplayName("Teklif Listeleme Testleri")
    class GetQuoteTests {

        @Test
        @DisplayName("Tüm teklifleri getirme")
        void whenGetAllQuotes_thenReturnAllQuotes() {
            // given
            List<Quote> quotes = List.of(testQuote);
            when(quoteRepository.findAllWithBrand()).thenReturn(quotes);

            // when
            List<Quote> result = quoteService.getAllQuotes();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testQuote);
            verify(quoteRepository).findAllWithBrand();
        }

        @Test
        @DisplayName("ID ile teklif getirme - başarılı")
        void whenGetQuoteById_thenReturnQuote() {
            // given
            Long quoteId = 1L;
            when(quoteRepository.findByIdWithBrandAndItems(quoteId)).thenReturn(Optional.of(testQuote));

            // when
            Quote result = quoteService.getQuoteById(quoteId);

            // then
            assertThat(result).isEqualTo(testQuote);
            verify(quoteRepository).findByIdWithBrandAndItems(quoteId);
        }

        @Test
        @DisplayName("ID ile teklif getirme - bulunamadı")
        void whenGetQuoteById_withNonExistentId_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            when(quoteRepository.findByIdWithBrandAndItems(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteService.getQuoteById(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Teklif bulunamadı");

            verify(quoteRepository).findByIdWithBrandAndItems(nonExistentId);
        }

        @Test
        @DisplayName("Markaya göre teklifleri getirme")
        void whenGetQuotesByBrand_thenReturnQuotes() {
            // given
            Long brandId = 1L;
            List<Quote> quotes = List.of(testQuote);
            when(brandRepository.findById(brandId)).thenReturn(Optional.of(testBrand));
            when(quoteRepository.findByBrandWithBrand(testBrand)).thenReturn(quotes);

            // when
            List<Quote> result = quoteService.getQuotesByBrand(brandId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testQuote);
            verify(brandRepository).findById(brandId);
            verify(quoteRepository).findByBrandWithBrand(testBrand);
        }
    }

    @Nested
    @DisplayName("Teklif Silme Testleri")
    class DeleteQuoteTests {

        @Test
        @DisplayName("DRAFT durumundaki teklifi silme - başarılı")
        void whenDeleteQuote_withDraftStatus_thenDeleteSuccessfully() {
            // given
            Long quoteId = 1L;
            Quote draftQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.DRAFT)
                    .items(new ArrayList<>())
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(draftQuote));
            doNothing().when(quoteRepository).delete(any(Quote.class));

            // when
            quoteService.deleteQuote(quoteId);

            // then
            verify(quoteRepository).findById(quoteId);
            verify(quoteRepository).delete(draftQuote);
        }

        @Test
        @DisplayName("EXPIRED durumundaki teklifi silme - başarılı")
        void whenDeleteQuote_withExpiredStatus_thenDeleteSuccessfully() {
            // given
            Long quoteId = 1L;
            Quote expiredQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.EXPIRED)
                    .items(new ArrayList<>())
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(expiredQuote));
            doNothing().when(quoteRepository).delete(any(Quote.class));

            // when
            quoteService.deleteQuote(quoteId);

            // then
            verify(quoteRepository).findById(quoteId);
            verify(quoteRepository).delete(expiredQuote);
        }

        @Test
        @DisplayName("Silinemez durumda teklifi silme - hata")
        void whenDeleteQuote_withUndeletableStatus_thenThrowException() {
            // given
            Long quoteId = 1L;
            Quote acceptedQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.ACCEPTED)
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(acceptedQuote));

            // when & then
            assertThatThrownBy(() -> quoteService.deleteQuote(quoteId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Bu teklif silinemez");

            verify(quoteRepository).findById(quoteId);
            verify(quoteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Olmayan teklifi silme - hata")
        void whenDeleteQuote_withNonExistentId_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            when(quoteRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteService.deleteQuote(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Teklif bulunamadı");

            verify(quoteRepository).findById(nonExistentId);
            verify(quoteRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Teklif Expire Etme Testleri")
    class ExpireQuoteTests {

        @Test
        @DisplayName("OFFER_SENT durumundaki teklifi expire etme - başarılı")
        void whenExpireQuote_withOfferSentStatus_thenExpireSuccessfully() {
            // given
            Long quoteId = 1L;
            Quote offerSentQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.OFFER_SENT)
                    .validUntil(LocalDate.now().plusDays(30))
                    .build();

            BrandProcess brandProcess = BrandProcess.builder()
                    .brand(testBrand)
                    .status(ProcessStatus.EXPIRED)
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(offerSentQuote));
            when(brandProcessService.checkForExpired(testBrand.getId()))
                    .thenReturn(brandProcess);
            doNothing().when(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    eq(brandProcess), eq(ProcessStatus.EXPIRED), eq(ProcessStatus.OFFER_SENT), anyString());

            // when
            quoteService.expireQuote(quoteId);

            // then
            assertThat(offerSentQuote.getStatus()).isEqualTo(QuoteStatus.EXPIRED);
            assertThat(offerSentQuote.getUpdatedAt()).isNotNull();

            verify(quoteRepository).findById(quoteId);
            verify(brandProcessService).checkForExpired(testBrand.getId());
            verify(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    eq(brandProcess), eq(ProcessStatus.EXPIRED), eq(ProcessStatus.OFFER_SENT), anyString());
        }

        @Test
        @DisplayName("Expire edilemeyen durumda teklifi expire etme - hata")
        void whenExpireQuote_withUnexpirableStatus_thenThrowException() {
            // given
            Long quoteId = 1L;
            Quote acceptedQuote = Quote.builder()
                    .id(quoteId)
                    .brand(testBrand)
                    .status(QuoteStatus.ACCEPTED)
                    .build();

            when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(acceptedQuote));

            // when & then
            assertThatThrownBy(() -> quoteService.expireQuote(quoteId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Teklif süresi dolmuş");

            verify(quoteRepository).findById(quoteId);
            verify(brandProcessService, never()).checkForExpired(any());
            verify(brandProcessHistoryService, never()).saveProcessHistoryForChangeStatus(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Olmayan teklifi expire etme - hata")
        void whenExpireQuote_withNonExistentId_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            when(quoteRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteService.expireQuote(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Teklif bulunamadı");

            verify(quoteRepository).findById(nonExistentId);
            verify(brandProcessService, never()).updateBrandProcessStatus(any(), any());
            verify(brandProcessHistoryService, never()).saveProcessHistoryForChangeStatus(any(), any(), any(), any());
        }
    }
} 