package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("OrderRepository Test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;
    private Brand testBrand;
    private Quote testQuote;
    private Factory testFactory;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();
        
        entityManager.persist(testBrand);

        testQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2500))
                .items(new ArrayList<>())
                .build();
        
        entityManager.persist(testQuote);

        testFactory = Factory.builder()
                .name("Test Factory")
                .address("Test Address")
                .dailyCapacity(1000)
                .active(true)
                .build();
        
        entityManager.persist(testFactory);

        Product testProduct = Product.builder()
                .name("Test Product")
                .code("TEST_PROD")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .active(true)
                .build();
        
        entityManager.persist(testProduct);

        testOrder = Order.builder()
                .quote(testQuote)
                .factory(testFactory)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.PENDING)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(14))
                .status(OrderItemStatus.PENDING)
                .build();

        testOrder.getItems().add(orderItem);
        
        entityManager.persistAndFlush(testOrder);
        entityManager.persistAndFlush(orderItem);
    }

    @Test
    @DisplayName("Sipariş ID'ye göre bulma")
    void whenFindById_thenReturnOrder() {
        // when
        Optional<Order> result = orderRepository.findById(testOrder.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.get().getTotalPrice()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(result.get().getFactory().getName()).isEqualTo("Test Factory");
        assertThat(result.get().getQuote().getBrand().getName()).isEqualTo("Test Brand");
    }

    @Test
    @DisplayName("Güncellemeler için sipariş bulma (findByIdForUpdate)")
    void whenFindByIdForUpdate_thenReturnOrderWithLock() {
        // when
        Optional<Order> result = orderRepository.findByIdForUpdate(testOrder.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.get().getTotalPrice()).isEqualTo(BigDecimal.valueOf(2500));
        
        // Quote ve items fetch edilmiş olmalı
        assertThat(result.get().getQuote()).isNotNull();
        assertThat(result.get().getQuote().getBrand()).isNotNull();
        assertThat(result.get().getItems()).isNotEmpty();
        assertThat(result.get().getItems().get(0).getProduct()).isNotNull();
    }

    @Test
    @DisplayName("Olmayan sipariş ID'ye göre arama")
    void whenFindById_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<Order> result = orderRepository.findById(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Olmayan sipariş ID'ye göre güncellemeler için arama")
    void whenFindByIdForUpdate_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<Order> result = orderRepository.findByIdForUpdate(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Sipariş kaydetme")
    void whenSave_thenReturnSavedOrder() {
        // given
        Quote newQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(45))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1500))
                .items(new ArrayList<>())
                .build();
        
        entityManager.persist(newQuote);
        
        Order newOrder = Order.builder()
                .quote(newQuote)
                .factory(null)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(1500))
                .status(OrderStatus.PENDING)
                .build();

        // when
        Order result = orderRepository.save(newOrder);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(result.getQuote()).isEqualTo(newQuote);
    }

    @Test
    @DisplayName("Sipariş silme")
    void whenDelete_thenOrderDeleted() {
        // given
        Quote deleteQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(60))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(500))
                .items(new ArrayList<>())
                .build();
        
        entityManager.persist(deleteQuote);
        
        Order saved = entityManager.persistAndFlush(Order.builder()
                .quote(deleteQuote)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .totalPrice(BigDecimal.valueOf(500))
                .status(OrderStatus.PENDING)
                .build());

        // when
        orderRepository.deleteById(saved.getId());
        entityManager.flush();

        // then
        Optional<Order> result = orderRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }
} 