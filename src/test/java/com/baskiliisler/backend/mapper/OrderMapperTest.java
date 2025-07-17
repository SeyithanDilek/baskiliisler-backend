package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
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

class OrderMapperTest {

    private Order orderWithFactory;
    private Order orderWithoutFactory;
    private Factory testFactory;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Quote testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(1))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2500))
                .items(new ArrayList<>())
                .build();

        testFactory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address")
                .phoneNumber("+90 555 123 45 67")
                .active(true)
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

        OrderItem orderItem1 = OrderItem.builder()
                .id(1L)
                .product(product1)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(14))
                .status(OrderItemStatus.PENDING)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .id(2L)
                .product(product2)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(200))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(21))
                .status(OrderItemStatus.PENDING)
                .build();

        orderItems = List.of(orderItem1, orderItem2);

        orderWithFactory = Order.builder()
                .id(1L)
                .quote(testQuote)
                .factory(testFactory)
                .items(new ArrayList<>(orderItems))
                .createdAt(LocalDateTime.now().minusDays(1))
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.IN_PRODUCTION)
                .build();

        orderWithoutFactory = Order.builder()
                .id(2L)
                .quote(testQuote)
                .factory(null)
                .items(new ArrayList<>(orderItems))
                .createdAt(LocalDateTime.now().minusDays(1))
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Fabrika atanmış sipariş DTO'ya dönüştürülünce factory bilgisi bulunmalı")
    void givenOrderWithFactory_whenConvertToDto_thenShouldIncludeFactoryInfo() {
        // When
        OrderResponseDto result = OrderMapper.toDto(orderWithFactory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orderWithFactory.getId());
        assertThat(result.status()).isEqualTo(orderWithFactory.getStatus());
        assertThat(result.totalPrice()).isEqualTo(orderWithFactory.getTotalPrice());
        
        // Brand bilgisi kontrolü
        assertThat(result.brand()).isNotNull();
        assertThat(result.brand().id()).isEqualTo(1L);
        assertThat(result.brand().name()).isEqualTo("Test Brand");
        
        // Factory bilgisi kontrolü
        assertThat(result.factory()).isNotNull();
        assertThat(result.factory().id()).isEqualTo(testFactory.getId());
        assertThat(result.factory().name()).isEqualTo(testFactory.getName());
        
        // Items kontrolü
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).productName()).isEqualTo("Test Product 1");
        assertThat(result.items().get(1).productName()).isEqualTo("Test Product 2");
    }

    @Test
    @DisplayName("Fabrika atanmamış sipariş DTO'ya dönüştürülünce factory bilgisi null olmalı")
    void givenOrderWithoutFactory_whenConvertToDto_thenFactoryInfoShouldBeNull() {
        // When
        OrderResponseDto result = OrderMapper.toDto(orderWithoutFactory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orderWithoutFactory.getId());
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.factory()).isNull();
        
        // Brand bilgisi hala mevcut olmalı
        assertThat(result.brand()).isNotNull();
        assertThat(result.brand().name()).isEqualTo("Test Brand");
        
        // Items kontrolü
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("Null Order için null dönmeli")
    void givenNullOrder_whenConvertToDto_thenShouldReturnNull() {
        // When
        OrderResponseDto result = OrderMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }
} 