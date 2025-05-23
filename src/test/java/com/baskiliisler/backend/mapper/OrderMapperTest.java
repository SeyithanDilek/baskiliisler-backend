package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
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
                .dailyCapacity(1000)
                .active(true)
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
                .unitPrice(BigDecimal.valueOf(300))
                .lineTotal(BigDecimal.valueOf(1500))
                .plannedDelivery(LocalDate.now().plusDays(21))
                .status(OrderItemStatus.READY)
                .build();

        orderItems = List.of(orderItem1, orderItem2);

        orderWithFactory = Order.builder()
                .id(1L)
                .quote(testQuote)
                .factory(testFactory)
                .items(new ArrayList<>(orderItems))
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.PENDING)
                .build();

        orderWithoutFactory = Order.builder()
                .id(2L)
                .quote(testQuote)
                .factory(null)
                .items(new ArrayList<>(orderItems))
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(45))
                .deliveredAt(LocalDateTime.now().plusDays(20))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.DELIVERED)
                .build();

        // OrderItem'ların order reference'larını set et
        orderItems.forEach(item -> item.setOrder(orderWithFactory));
    }

    @Test
    @DisplayName("Factory bilgisi olan siparişi DTO'ya dönüştürme")
    void whenToDto_withFactory_thenReturnCompleteOrderResponseDto() {
        // when
        OrderResponseDto result = OrderMapper.toDto(orderWithFactory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.createdAt()).isEqualTo(orderWithFactory.getCreatedAt());
        assertThat(result.deadline()).isEqualTo(orderWithFactory.getDeadline());
        assertThat(result.deliveredAt()).isEqualTo(orderWithFactory.getDeliveredAt());
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(2500));

        // Factory bilgisi kontrolü
        assertThat(result.factory()).isNotNull();
        assertThat(result.factory().id()).isEqualTo(1L);
        assertThat(result.factory().name()).isEqualTo("Test Factory");

        // Items kontrolü
        assertThat(result.items()).hasSize(2);
        
        OrderResponseDto.ItemResp firstItem = result.items().get(0);
        assertThat(firstItem.productId()).isEqualTo(1L);
        assertThat(firstItem.productName()).isEqualTo("Test Product 1");
        assertThat(firstItem.quantity()).isEqualTo(10);
        assertThat(firstItem.unitPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(firstItem.lineTotal()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(firstItem.plannedDelivery()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(firstItem.status()).isEqualTo(OrderItemStatus.PENDING);
    }

    @Test
    @DisplayName("Factory bilgisi olmayan siparişi DTO'ya dönüştürme")
    void whenToDto_withoutFactory_thenReturnOrderResponseDtoWithNullFactory() {
        // when
        OrderResponseDto result = OrderMapper.toDto(orderWithoutFactory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(result.createdAt()).isEqualTo(orderWithoutFactory.getCreatedAt());
        assertThat(result.deadline()).isEqualTo(orderWithoutFactory.getDeadline());
        assertThat(result.deliveredAt()).isEqualTo(orderWithoutFactory.getDeliveredAt());
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.valueOf(2500));

        // Factory bilgisi null olmalı
        assertThat(result.factory()).isNull();

        // Items kontrolü
        assertThat(result.items()).hasSize(2);
    }

    @Test
    @DisplayName("Boş item listesi olan siparişi DTO'ya dönüştürme")
    void whenToDto_withEmptyItems_thenReturnOrderResponseDtoWithEmptyItems() {
        // given
        Order orderWithEmptyItems = Order.builder()
                .id(3L)
                .quote(orderWithFactory.getQuote())
                .factory(testFactory)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(60))
                .totalPrice(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        // when
        OrderResponseDto result = OrderMapper.toDto(orderWithEmptyItems);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.totalPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.factory()).isNotNull();
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("Null plannedDelivery olan item'ları DTO'ya dönüştürme")
    void whenToDto_withNullPlannedDeliveryItems_thenReturnCorrectDto() {
        // given
        Product product = Product.builder()
                .id(3L)
                .name("Test Product 3")
                .code("TEST_PROD_3")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(50))
                .build();

        OrderItem itemWithNullDelivery = OrderItem.builder()
                .id(3L)
                .product(product)
                .quantity(20)
                .unitPrice(BigDecimal.valueOf(50))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(null)
                .status(OrderItemStatus.PENDING)
                .build();

        Order orderWithNullDeliveryItem = Order.builder()
                .id(4L)
                .quote(orderWithFactory.getQuote())
                .factory(null)
                .items(List.of(itemWithNullDelivery))
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(1000))
                .status(OrderStatus.PENDING)
                .build();

        itemWithNullDelivery.setOrder(orderWithNullDeliveryItem);

        // when
        OrderResponseDto result = OrderMapper.toDto(orderWithNullDeliveryItem);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        
        OrderResponseDto.ItemResp item = result.items().get(0);
        assertThat(item.productId()).isEqualTo(3L);
        assertThat(item.productName()).isEqualTo("Test Product 3");
        assertThat(item.plannedDelivery()).isNull();
        assertThat(item.status()).isEqualTo(OrderItemStatus.PENDING);
    }
} 