package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import com.baskiliisler.backend.type.Unit;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("OrderItemRepository Test")
class OrderItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private OrderItem testOrderItem;
    private Order testOrder;
    private Product testProduct;
    private Dealer testDealer;

    @BeforeEach
    void setUp() {
        testDealer = Dealer.builder()
                .name("Test Dealer")
                .active(true)
                .build();
        entityManager.persist(testDealer);
        entityManager.flush();

        Brand testBrand = Brand.builder()
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .dealer(testDealer)
                .build();
        
        entityManager.persist(testBrand);

        Quote testQuote = Quote.builder()
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1000))
                .items(new ArrayList<>())
                .dealer(testDealer)
                .build();
        
        entityManager.persist(testQuote);

        testOrder = Order.builder()
                .quote(testQuote)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(1000))
                .status(OrderStatus.PENDING)
                .dealer(testDealer)
                .build();
        
        entityManager.persist(testOrder);

        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Product açıklaması")
                .unit(Unit.ADET)
                .unitPrice(BigDecimal.valueOf(100))
                .taxRate(BigDecimal.valueOf(18.00))
                .active(true)
                .dealer(testDealer)
                .build();

        entityManager.persist(testProduct);

        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(500))
                .plannedDelivery(LocalDate.now().plusDays(14))
                .status(OrderItemStatus.PENDING)
                .build();

        entityManager.persistAndFlush(testOrderItem);
    }

    @Test
    @DisplayName("Order item ID'ye göre bulma")
    void whenFindById_thenReturnOrderItem() {
        // when
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(5);
        assertThat(result.get().getUnitPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.get().getLineTotal()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(result.get().getStatus()).isEqualTo(OrderItemStatus.PENDING);
        assertThat(result.get().getProduct().getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Olmayan order item ID'ye göre arama")
    void whenFindById_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<OrderItem> result = orderItemRepository.findById(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Tüm order item'ları listeleme")
    void whenFindAll_thenReturnAllOrderItems() {
        // when
        List<OrderItem> result = orderItemRepository.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(5);
        assertThat(result.get(0).getProduct().getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Yeni order item kaydetme")
    void whenSave_thenReturnSavedOrderItem() {
        // given
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New Product açıklaması")
                .unit(Unit.LITRE)
                .unitPrice(BigDecimal.valueOf(50))
                .taxRate(BigDecimal.valueOf(18.00))
                .active(true)
                .dealer(testDealer)
                .build();
        
        entityManager.persist(newProduct);

        OrderItem newOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(newProduct)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(50))
                .lineTotal(BigDecimal.valueOf(500))
                .plannedDelivery(LocalDate.now().plusDays(30))
                .status(OrderItemStatus.PENDING)
                .build();

        // when
        OrderItem savedOrderItem = orderItemRepository.save(newOrderItem);

        // then
        assertThat(savedOrderItem.getId()).isNotNull();
        assertThat(savedOrderItem.getQuantity()).isEqualTo(10);
        assertThat(savedOrderItem.getProduct().getName()).isEqualTo("New Product");
        assertThat(savedOrderItem.getOrder().getId()).isEqualTo(testOrder.getId());
    }

    @Test
    @DisplayName("Sipariş kalemi durumu güncelleme")
    void whenUpdateStatus_thenStatusChanged() {
        // given
        testOrderItem.setStatus(OrderItemStatus.DELIVERED);

        // when
        OrderItem result = orderItemRepository.save(testOrderItem);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.DELIVERED);
        
        // Database'den tekrar oku
        Optional<OrderItem> updated = orderItemRepository.findById(testOrderItem.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(OrderItemStatus.DELIVERED);
    }

    @Test
    @DisplayName("Sipariş kalemi silme")
    void whenDelete_thenOrderItemDeleted() {
        // given
        OrderItem saved = entityManager.persistAndFlush(OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(15)
                .unitPrice(BigDecimal.valueOf(75))
                .lineTotal(BigDecimal.valueOf(1125))
                .plannedDelivery(LocalDate.now().plusDays(18))
                .status(OrderItemStatus.PENDING)
                .build());

        // when
        orderItemRepository.deleteById(saved.getId());
        entityManager.flush();

        // then
        Optional<OrderItem> result = orderItemRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }
} 