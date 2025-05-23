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

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
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
                .build();
        
        entityManager.persist(testQuote);

        testOrder = Order.builder()
                .quote(testQuote)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(1000))
                .status(OrderStatus.PENDING)
                .build();
        
        entityManager.persist(testOrder);

        testProduct = Product.builder()
                .name("Test Product")
                .code("TEST_PROD")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .active(true)
                .build();
        
        entityManager.persist(testProduct);

        testOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(testProduct)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(14))
                .status(OrderItemStatus.PENDING)
                .build();

        entityManager.persistAndFlush(testOrderItem);
    }

    @Test
    @DisplayName("Sipariş kalemi ID'ye göre bulma")
    void whenFindById_thenReturnOrderItem() {
        // when
        Optional<OrderItem> result = orderItemRepository.findById(testOrderItem.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(10);
        assertThat(result.get().getUnitPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.get().getLineTotal()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.get().getStatus()).isEqualTo(OrderItemStatus.PENDING);
        assertThat(result.get().getProduct().getName()).isEqualTo("Test Product");
        assertThat(result.get().getOrder().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Olmayan sipariş kalemi ID'ye göre arama")
    void whenFindById_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<OrderItem> result = orderItemRepository.findById(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Tüm sipariş kalemlerini listeleme")
    void whenFindAll_thenReturnAllOrderItems() {
        // given
        Product product2 = Product.builder()
                .name("Product 2")
                .code("PROD_2")
                .unit("kg")
                .unitPrice(BigDecimal.valueOf(200))
                .active(true)
                .build();
        
        entityManager.persist(product2);

        OrderItem orderItem2 = OrderItem.builder()
                .order(testOrder)
                .product(product2)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(200))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(20))
                .status(OrderItemStatus.READY)
                .build();

        entityManager.persistAndFlush(orderItem2);

        // when
        List<OrderItem> result = orderItemRepository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderItem::getQuantity)
                .containsExactlyInAnyOrder(10, 5);
        assertThat(result).extracting(OrderItem::getStatus)
                .containsExactlyInAnyOrder(OrderItemStatus.PENDING, OrderItemStatus.READY);
        assertThat(result).extracting(item -> item.getProduct().getName())
                .containsExactlyInAnyOrder("Test Product", "Product 2");
    }

    @Test
    @DisplayName("Sipariş kalemi kaydetme")
    void whenSave_thenReturnSavedOrderItem() {
        // given
        Product newProduct = Product.builder()
                .name("New Product")
                .code("NEW_PROD")
                .unit("lt")
                .unitPrice(BigDecimal.valueOf(50))
                .active(true)
                .build();
        
        entityManager.persist(newProduct);

        OrderItem newOrderItem = OrderItem.builder()
                .order(testOrder)
                .product(newProduct)
                .quantity(20)
                .unitPrice(BigDecimal.valueOf(50))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(25))
                .status(OrderItemStatus.PENDING)
                .build();

        // when
        OrderItem result = orderItemRepository.save(newOrderItem);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(20);
        assertThat(result.getUnitPrice()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(result.getLineTotal()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(result.getStatus()).isEqualTo(OrderItemStatus.PENDING);
        assertThat(result.getProduct().getName()).isEqualTo("New Product");
        assertThat(result.getOrder()).isEqualTo(testOrder);
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