package com.baskiliisler.backend.scheduler;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.repository.QuoteRepository;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.type.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteExpirationSchedulerTest {

    @Mock
    private QuoteService quoteService;

    @Mock
    private QuoteRepository quoteRepo;

    @InjectMocks
    private QuoteExpirationScheduler quoteExpirationScheduler;

    private List<Quote> expiredQuotes;
    private Brand testBrand;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Quote expiredQuote1 = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().minusDays(1)) // Dün süresi dolmuş
                .createdAt(LocalDateTime.now().minusDays(5))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1000))
                .items(new ArrayList<>())
                .build();

        Quote expiredQuote2 = Quote.builder()
                .id(2L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().minusDays(3)) // 3 gün önce süresi dolmuş
                .createdAt(LocalDateTime.now().minusDays(10))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(new ArrayList<>())
                .build();

        Quote expiredQuote3 = Quote.builder()
                .id(3L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().minusWeeks(1)) // 1 hafta önce süresi dolmuş
                .createdAt(LocalDateTime.now().minusWeeks(2))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(3000))
                .items(new ArrayList<>())
                .build();

        expiredQuotes = Arrays.asList(expiredQuote1, expiredQuote2, expiredQuote3);
    }

    @Test
    @DisplayName("Süresi dolmuş teklifleri iptal etme - birden fazla teklif var")
    void whenExpireOutdatedQuotes_withMultipleExpiredQuotes_thenExpireAllQuotes() {
        // given
        LocalDate today = LocalDate.now();
        when(quoteRepo.findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today))
                .thenReturn(expiredQuotes);
        doNothing().when(quoteService).expireQuote(any(Long.class));

        // when
        quoteExpirationScheduler.expireOutdatedQuotes();

        // then
        verify(quoteRepo).findByStatusAndValidUntilBefore(eq(QuoteStatus.OFFER_SENT), eq(today));
        verify(quoteService, times(3)).expireQuote(any(Long.class));
        verify(quoteService).expireQuote(1L);
        verify(quoteService).expireQuote(2L);
        verify(quoteService).expireQuote(3L);
    }

    @Test
    @DisplayName("Süresi dolmuş teklifleri iptal etme - tek teklif var")
    void whenExpireOutdatedQuotes_withSingleExpiredQuote_thenExpireThatQuote() {
        // given
        LocalDate today = LocalDate.now();
        List<Quote> singleExpiredQuote = List.of(expiredQuotes.get(0));
        when(quoteRepo.findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today))
                .thenReturn(singleExpiredQuote);
        doNothing().when(quoteService).expireQuote(any(Long.class));

        // when
        quoteExpirationScheduler.expireOutdatedQuotes();

        // then
        verify(quoteRepo).findByStatusAndValidUntilBefore(eq(QuoteStatus.OFFER_SENT), eq(today));
        verify(quoteService, times(1)).expireQuote(any(Long.class));
        verify(quoteService).expireQuote(1L);
    }

    @Test
    @DisplayName("Süresi dolmuş teklifleri iptal etme - hiç süresi dolmamış teklif yok")
    void whenExpireOutdatedQuotes_withNoExpiredQuotes_thenNoQuotesExpired() {
        // given
        LocalDate today = LocalDate.now();
        when(quoteRepo.findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today))
                .thenReturn(new ArrayList<>());

        // when
        quoteExpirationScheduler.expireOutdatedQuotes();

        // then
        verify(quoteRepo).findByStatusAndValidUntilBefore(eq(QuoteStatus.OFFER_SENT), eq(today));
        verify(quoteService, never()).expireQuote(any(Long.class));
    }

    @Test
    @DisplayName("Süresi dolmuş teklifleri iptal etme - null liste")
    void whenExpireOutdatedQuotes_withNullList_thenNoQuotesExpired() {
        // given
        LocalDate today = LocalDate.now();
        when(quoteRepo.findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today))
                .thenReturn(null);

        // when
        quoteExpirationScheduler.expireOutdatedQuotes();

        // then
        verify(quoteRepo).findByStatusAndValidUntilBefore(eq(QuoteStatus.OFFER_SENT), eq(today));
        verify(quoteService, never()).expireQuote(any(Long.class));
    }

    @Test
    @DisplayName("Süresi dolmuş teklifleri iptal etme - service exception durumunda")
    void whenExpireOutdatedQuotes_withServiceException_thenContinueWithOtherQuotes() {
        // given
        LocalDate today = LocalDate.now();
        when(quoteRepo.findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today))
                .thenReturn(expiredQuotes);
        
        // İlk teklif için exception fırlatıyor, diğerleri için normal davranıyor
        doThrow(new RuntimeException("Service error")).when(quoteService).expireQuote(1L);
        doNothing().when(quoteService).expireQuote(2L);
        doNothing().when(quoteService).expireQuote(3L);

        // when
        quoteExpirationScheduler.expireOutdatedQuotes();

        // then
        verify(quoteRepo).findByStatusAndValidUntilBefore(eq(QuoteStatus.OFFER_SENT), eq(today));
        verify(quoteService).expireQuote(1L);
        verify(quoteService).expireQuote(2L);
        verify(quoteService).expireQuote(3L);
    }
} 