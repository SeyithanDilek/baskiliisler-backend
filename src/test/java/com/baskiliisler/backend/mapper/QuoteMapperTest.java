package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.QuoteStatus;
import com.baskiliisler.backend.type.Unit;
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
                .description("Test Product 1 açıklaması")
                .unit(Unit.ADET)
                .unitPrice(BigDecimal.valueOf(100))
                .taxRate(BigDecimal.valueOf(18.00))
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Test Product 2")
                .description("Test Product 2 açıklaması")
                .unit(Unit.KG)
                .unitPrice(BigDecimal.valueOf(200))
                .taxRate(BigDecimal.valueOf(18.00))
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
                .totalPrice(BigDecimal.valueOf(2000))
                .currency("TRY")
                .validUntil(LocalDate.now().plusDays(30))
                .status(QuoteStatus.OFFER_SENT)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>(List.of(quoteItem1, quoteItem2)))
                .build();

        quoteWithoutItems = Quote.builder()
                .id(2L)
                .brand(testBrand)
                .totalPrice(BigDecimal.valueOf(0))
                .currency("TRY")
                .validUntil(LocalDate.now().plusDays(30))
                .status(QuoteStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Items içeren Quote DTO'ya dönüştürülünce doğru yapıya sahip olmalı")
    void givenQuoteWithItems_whenConvertToDto_thenShouldReturnCorrectDto() {
        // When
        QuoteResponseDto result = QuoteMapper.toDto(quoteWithItems);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(quoteWithItems.getId());
        assertThat(result.status()).isEqualTo(quoteWithItems.getStatus());
        assertThat(result.totalPrice()).isEqualTo(quoteWithItems.getTotalPrice());
        assertThat(result.brandName()).isEqualTo(quoteWithItems.getBrand().getName());
        assertThat(result.items()).hasSize(2);
        
        // Items kontrolü
        assertThat(result.items().get(0).productName()).isEqualTo("Test Product 1");
        assertThat(result.items().get(0).quantity()).isEqualTo(10);
        assertThat(result.items().get(1).productName()).isEqualTo("Test Product 2");
        assertThat(result.items().get(1).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Items içermeyen Quote DTO'ya dönüştürülünce boş items listesi olmalı")
    void givenQuoteWithoutItems_whenConvertToDto_thenShouldReturnEmptyItems() {
        // When
        QuoteResponseDto result = QuoteMapper.toDto(quoteWithoutItems);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(quoteWithoutItems.getId());
        assertThat(result.status()).isEqualTo(QuoteStatus.DRAFT);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Null Quote için null dönmeli")
    void givenNullQuote_whenConvertToDto_thenShouldReturnNull() {
        // When
        QuoteResponseDto result = QuoteMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }
} 