package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuoteMapper Test")
class QuoteMapperTest {

    private Quote quoteWithItems;
    private Quote quoteWithoutItems;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Product product1 = Product.builder()
                .id(1L)
                .name("Test Product 1")
                .code("TEST_PROD_1")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Test Product 2")
                .code("TEST_PROD_2")
                .unit("kg")
                .unitPrice(BigDecimal.valueOf(200))
                .build();

        QuoteItem quoteItem1 = QuoteItem.builder()
                .id(1L)
                .product(product1)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(1000))
                .build();

        QuoteItem quoteItem2 = QuoteItem.builder()
                .id(2L)
                .product(product2)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(200))
                .lineTotal(BigDecimal.valueOf(1000))
                .build();

        quoteWithItems = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(List.of(quoteItem1, quoteItem2))
                .build();

        quoteWithoutItems = Quote.builder()
                .id(2L)
                .brand(testBrand)
                .status(QuoteStatus.DRAFT)
                .validUntil(LocalDate.now().plusDays(15))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Kalemli teklifi DTO'ya dönüştürme")
    void whenToDto_withItems_thenReturnCompleteQuoteResponseDto() {
        // when
        QuoteResponseDto result = QuoteMapper.toDto(quoteWithItems);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(QuoteStatus.OFFER_SENT);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(2000));
        assertThat(result.validUntil()).isEqualTo(quoteWithItems.getValidUntil());

        // Items kontrolü
        assertThat(result.items()).hasSize(2);
        
        QuoteResponseDto.QuoteItemResp firstItem = result.items().get(0);
        assertThat(firstItem.productId()).isEqualTo(1L);
        assertThat(firstItem.productName()).isEqualTo("Test Product 1");
        assertThat(firstItem.quantity()).isEqualTo(10);
        assertThat(firstItem.unitPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(firstItem.lineTotal()).isEqualTo(BigDecimal.valueOf(1000));

        QuoteResponseDto.QuoteItemResp secondItem = result.items().get(1);
        assertThat(secondItem.productId()).isEqualTo(2L);
        assertThat(secondItem.productName()).isEqualTo("Test Product 2");
        assertThat(secondItem.quantity()).isEqualTo(5);
        assertThat(secondItem.unitPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(secondItem.lineTotal()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Kalemsiz teklifi DTO'ya dönüştürme")
    void whenToDto_withoutItems_thenReturnQuoteResponseDtoWithEmptyItems() {
        // when
        QuoteResponseDto result = QuoteMapper.toDto(quoteWithoutItems);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.validUntil()).isEqualTo(quoteWithoutItems.getValidUntil());
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("ACCEPTED durumunda teklifi DTO'ya dönüştürme")
    void whenToDto_withAcceptedStatus_thenReturnCorrectDto() {
        // given
        Quote acceptedQuote = Quote.builder()
                .id(3L)
                .brand(quoteWithItems.getBrand())
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(45))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(5000))
                .items(new ArrayList<>())
                .build();

        // when
        QuoteResponseDto result = QuoteMapper.toDto(acceptedQuote);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.status()).isEqualTo(QuoteStatus.ACCEPTED);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(result.validUntil()).isEqualTo(acceptedQuote.getValidUntil());
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("EXPIRED durumunda teklifi DTO'ya dönüştürme")
    void whenToDto_withExpiredStatus_thenReturnCorrectDto() {
        // given
        Quote expiredQuote = Quote.builder()
                .id(4L)
                .brand(quoteWithItems.getBrand())
                .status(QuoteStatus.EXPIRED)
                .validUntil(LocalDate.now().minusDays(5))  // Geçmiş tarih
                .createdAt(LocalDateTime.now().minusDays(30))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1500))
                .items(new ArrayList<>())
                .build();

        // when
        QuoteResponseDto result = QuoteMapper.toDto(expiredQuote);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.status()).isEqualTo(QuoteStatus.EXPIRED);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(result.validUntil()).isEqualTo(expiredQuote.getValidUntil());
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Farklı para birimi ile teklifi DTO'ya dönüştürme")
    void whenToDto_withDifferentCurrency_thenReturnCorrectDto() {
        // given
        Quote usdQuote = Quote.builder()
                .id(5L)
                .brand(quoteWithItems.getBrand())
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("USD")  // Farklı para birimi
                .totalPrice(BigDecimal.valueOf(250.50))
                .items(new ArrayList<>())
                .build();

        // when
        QuoteResponseDto result = QuoteMapper.toDto(usdQuote);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.status()).isEqualTo(QuoteStatus.OFFER_SENT);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(250.50));
        assertThat(result.validUntil()).isEqualTo(usdQuote.getValidUntil());
        assertThat(result.items()).isEmpty();
    }
} 