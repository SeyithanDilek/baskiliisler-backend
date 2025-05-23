package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.OrderRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderService orderService;

    private Quote testQuote;
    private Order testOrder;
    private Map<Long, LocalDate> testDeadlines;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Product testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .code("TEST_PROD")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .build();

        QuoteItem testQuoteItem = QuoteItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(500))
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(500))
                .items(new ArrayList<>())
                .build();
        
        testQuote.getItems().add(testQuoteItem);
        testQuoteItem.setQuote(testQuote);

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

        // Tüm testler için genel stub
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(orderItemService).assembleAndSaveOrderItems(any(Quote.class), any(Map.class), any(Order.class));
    }

    @Test
    @DisplayName("Tekliften sipariş oluşturma - teslim tarihleri ile")
    void whenCreateOrderFromQuote_withDeadlines_thenReturnCreatedOrder() {
        // when
        Order result = orderService.createOrderFromQuote(testQuote, testDeadlines);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuote()).isEqualTo(testQuote);
        assertThat(result.getTotalPrice()).isEqualTo(testQuote.getTotalPrice());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(orderRepository).save(any(Order.class));
        verify(orderItemService).assembleAndSaveOrderItems(eq(testQuote), eq(testDeadlines), any(Order.class));
    }

    @Test
    @DisplayName("Tekliften sipariş oluşturma - teslim tarihleri olmadan")
    void whenCreateOrderFromQuote_withoutDeadlines_thenReturnCreatedOrder() {
        // when
        Order result = orderService.createOrderFromQuote(testQuote, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuote()).isEqualTo(testQuote);
        assertThat(result.getTotalPrice()).isEqualTo(testQuote.getTotalPrice());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(orderRepository).save(any(Order.class));
        verify(orderItemService).assembleAndSaveOrderItems(eq(testQuote), isNull(), any(Order.class));
    }

    @Test
    @DisplayName("Tekliften sipariş oluşturma - boş teslim tarihleri haritası")
    void whenCreateOrderFromQuote_withEmptyDeadlines_thenReturnCreatedOrder() {
        // given
        Map<Long, LocalDate> emptyDeadlines = Map.of();

        // when
        Order result = orderService.createOrderFromQuote(testQuote, emptyDeadlines);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuote()).isEqualTo(testQuote);
        assertThat(result.getTotalPrice()).isEqualTo(testQuote.getTotalPrice());
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();

        verify(orderRepository).save(any(Order.class));
        verify(orderItemService).assembleAndSaveOrderItems(eq(testQuote), eq(emptyDeadlines), any(Order.class));
    }
} 