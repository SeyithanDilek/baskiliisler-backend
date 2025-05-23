package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.type.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class QuoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private QuoteRepository quoteRepository;

    private Brand testBrand;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();
        
        entityManager.persistAndFlush(testBrand);
    }

    @Test
    @DisplayName("Status ve valid until date'e göre teklifleri bulma")
    void whenFindByStatusAndValidUntilBefore_thenReturnExpiredQuotes() {
        // given
        Quote validQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(5)) // İleride
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1000))
                .items(new ArrayList<>())
                .build();

        Quote expiredQuote1 = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().minusDays(1)) // Dün süresi dolmuş
                .createdAt(LocalDateTime.now().minusDays(5))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(new ArrayList<>())
                .build();

        Quote expiredQuote2 = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().minusDays(3)) // 3 gün önce süresi dolmuş
                .createdAt(LocalDateTime.now().minusDays(7))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(3000))
                .items(new ArrayList<>())
                .build();

        Quote acceptedExpiredQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED) // Farklı status
                .validUntil(LocalDate.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(6))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(4000))
                .items(new ArrayList<>())
                .build();

        entityManager.persist(validQuote);
        entityManager.persist(expiredQuote1);
        entityManager.persist(expiredQuote2);
        entityManager.persist(acceptedExpiredQuote);
        entityManager.flush();

        // when
        List<Quote> result = quoteRepository.findByStatusAndValidUntilBefore(
                QuoteStatus.OFFER_SENT, LocalDate.now());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Quote::getTotalPrice)
                .containsExactlyInAnyOrder(
                        BigDecimal.valueOf(2000),
                        BigDecimal.valueOf(3000)
                );
        assertThat(result).allMatch(quote -> 
                quote.getStatus() == QuoteStatus.OFFER_SENT &&
                quote.getValidUntil().isBefore(LocalDate.now())
        );
    }

    @Test
    @DisplayName("Süresi dolmuş teklif yoksa boş liste döndürme")
    void whenFindByStatusAndValidUntilBefore_withNoExpiredQuotes_thenReturnEmptyList() {
        // given
        Quote validQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1000))
                .items(new ArrayList<>())
                .build();

        entityManager.persistAndFlush(validQuote);

        // when
        List<Quote> result = quoteRepository.findByStatusAndValidUntilBefore(
                QuoteStatus.OFFER_SENT, LocalDate.now());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Hiç teklif yoksa boş liste döndürme")
    void whenFindByStatusAndValidUntilBefore_withNoQuotes_thenReturnEmptyList() {
        // when
        List<Quote> result = quoteRepository.findByStatusAndValidUntilBefore(
                QuoteStatus.OFFER_SENT, LocalDate.now());

        // then
        assertThat(result).isEmpty();
    }
} 