package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.OrderItemRepository;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    private Quote testQuote;
    private Order testOrder;
    private Product testProduct1;
    private Product testProduct2;
    private QuoteItem testQuoteItem1;
    private QuoteItem testQuoteItem2;
    private Map<Long, LocalDate> testDeadlines;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testProduct1 = Product.builder()
                .id(1L)
                .name("Test Product 1")
                .code("TEST_PROD_1")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .name("Test Product 2")
                .code("TEST_PROD_2")
                .unit("kg")
                .unitPrice(BigDecimal.valueOf(200))
                .build();

        testQuoteItem1 = QuoteItem.builder()
                .id(1L)
                .product(testProduct1)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(500))
                .build();

        testQuoteItem2 = QuoteItem.builder()
                .id(2L)
                .product(testProduct2)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(200))
                .lineTotal(BigDecimal.valueOf(600))
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1100))
                .items(new ArrayList<>())
                .build();

        testQuote.getItems().add(testQuoteItem1);
        testQuote.getItems().add(testQuoteItem2);
        testQuoteItem1.setQuote(testQuote);
        testQuoteItem2.setQuote(testQuote);

        testOrder = Order.builder()
                .id(1L)
                .quote(testQuote)
                .createdAt(LocalDateTime.now())
                .totalPrice(testQuote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        testDeadlines = Map.of(
                1L, LocalDate.now().plusDays(14),
                2L, LocalDate.now().plusDays(21)
        );
    }

    @Test
    @DisplayName("Teklif kalemlerinden sipariş kalemleri oluşturma - teslim tarihleri ile")
    void whenAssembleAndSaveOrderItems_withDeadlines_thenSaveOrderItems() {
        // given
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        orderItemService.assembleAndSaveOrderItems(testQuote, testDeadlines, testOrder);

        // then
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        
        // İlk kalem için doğrulama
        verify(orderItemRepository).save(argThat(orderItem -> 
                orderItem.getOrder().equals(testOrder) &&
                orderItem.getProduct().equals(testProduct1) &&
                orderItem.getQuantity().equals(5) &&
                orderItem.getUnitPrice().equals(BigDecimal.valueOf(100)) &&
                orderItem.getLineTotal().equals(BigDecimal.valueOf(500)) &&
                orderItem.getPlannedDelivery().equals(testDeadlines.get(1L)) &&
                orderItem.getStatus().equals(OrderItemStatus.PENDING)
        ));

        // İkinci kalem için doğrulama
        verify(orderItemRepository).save(argThat(orderItem -> 
                orderItem.getOrder().equals(testOrder) &&
                orderItem.getProduct().equals(testProduct2) &&
                orderItem.getQuantity().equals(3) &&
                orderItem.getUnitPrice().equals(BigDecimal.valueOf(200)) &&
                orderItem.getLineTotal().equals(BigDecimal.valueOf(600)) &&
                orderItem.getPlannedDelivery().equals(testDeadlines.get(2L)) &&
                orderItem.getStatus().equals(OrderItemStatus.PENDING)
        ));
    }

    @Test
    @DisplayName("Teklif kalemlerinden sipariş kalemleri oluşturma - teslim tarihleri olmadan")
    void whenAssembleAndSaveOrderItems_withoutDeadlines_thenSaveOrderItemsWithoutDeadlines() {
        // given
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        orderItemService.assembleAndSaveOrderItems(testQuote, null, testOrder);

        // then
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        
        // Teslim tarihleri null olmalı
        verify(orderItemRepository, times(2)).save(argThat(orderItem -> 
                orderItem.getPlannedDelivery() == null
        ));
    }

    @Test
    @DisplayName("Teklif kalemlerinden sipariş kalemleri oluşturma - kısmi teslim tarihleri")
    void whenAssembleAndSaveOrderItems_withPartialDeadlines_thenSaveOrderItemsCorrectly() {
        // given
        Map<Long, LocalDate> partialDeadlines = Map.of(
                1L, LocalDate.now().plusDays(14)
                // 2L için tarih yok
        );
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        orderItemService.assembleAndSaveOrderItems(testQuote, partialDeadlines, testOrder);

        // then
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        
        // İlk kalem için tarih var
        verify(orderItemRepository).save(argThat(orderItem -> 
                orderItem.getProduct().getId().equals(1L) &&
                orderItem.getPlannedDelivery().equals(partialDeadlines.get(1L))
        ));

        // İkinci kalem için tarih yok
        verify(orderItemRepository).save(argThat(orderItem -> 
                orderItem.getProduct().getId().equals(2L) &&
                orderItem.getPlannedDelivery() == null
        ));
    }

    @Test
    @DisplayName("Boş teklif kalemleri ile sipariş kalemleri oluşturma")
    void whenAssembleAndSaveOrderItems_withEmptyQuoteItems_thenNoOrderItemsSaved() {
        // given
        Quote emptyQuote = Quote.builder()
                .id(2L)
                .brand(testQuote.getBrand())
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // when
        orderItemService.assembleAndSaveOrderItems(emptyQuote, testDeadlines, testOrder);

        // then
        verify(orderItemRepository, never()).save(any(OrderItem.class));
    }
} 