package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.OrderRepository;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private FactoryService factoryService;

    @Mock
    private BrandProcessService brandProcessService;

    @Mock
    private BrandProcessHistoryService brandProcessHistoryService;

    @InjectMocks
    private OrderService orderService;

    private Brand testBrand;
    private Quote testQuote;
    private Order testOrder;
    private Factory testFactory;
    private Map<Long, LocalDate> testDeadlines;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
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

        testFactory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address")
                .dailyCapacity(100)
                .active(true)
                .build();

        testDeadlines = Map.of(
                1L, LocalDate.now().plusDays(14),
                2L, LocalDate.now().plusDays(21)
        );

        // Tüm testler için genel stub
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(orderItemService).assembleAndSaveOrderItems(any(Quote.class), any(Map.class), any(Order.class));
    }

    @Nested
    @DisplayName("Sipariş Oluşturma Testleri")
    class CreateOrderTests {

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

    @Nested
    @DisplayName("Sipariş Listeleme Testleri")
    class GetOrderTests {

        @Test
        @DisplayName("Tüm siparişleri getirme")
        void whenGetAllOrders_thenReturnAllOrders() {
            // given
            List<Order> orders = List.of(testOrder);
            when(orderRepository.findAll()).thenReturn(orders);

            // when
            List<Order> result = orderService.getAllOrders();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testOrder);
            verify(orderRepository).findAll();
        }

        @Test
        @DisplayName("ID ile sipariş getirme - başarılı")
        void whenGetOrderById_thenReturnOrder() {
            // given
            Long orderId = 1L;
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            // when
            Order result = orderService.getOrderById(orderId);

            // then
            assertThat(result).isEqualTo(testOrder);
            verify(orderRepository).findById(orderId);
        }

        @Test
        @DisplayName("ID ile sipariş getirme - bulunamadı")
        void whenGetOrderById_withNonExistentId_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrderById(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Sipariş bulunamadı");

            verify(orderRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("Markaya göre siparişleri getirme")
        void whenGetOrdersByBrand_thenReturnOrders() {
            // given
            Long brandId = 1L;
            List<Order> orders = List.of(testOrder);
            when(orderRepository.findByQuoteBrandId(brandId)).thenReturn(orders);

            // when
            List<Order> result = orderService.getOrdersByBrand(brandId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testOrder);
            verify(orderRepository).findByQuoteBrandId(brandId);
        }
    }

    @Nested
    @DisplayName("Sipariş Durumu Güncelleme Testleri")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Sipariş durumu güncelleme - başarılı")
        void whenUpdateOrderStatus_thenReturnUpdatedOrder() {
            // given
            Long orderId = 1L;
            OrderStatus newStatus = OrderStatus.IN_PRODUCTION;
            
            Order updatedOrder = Order.builder()
                    .id(orderId)
                    .quote(testQuote)
                    .createdAt(testOrder.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .totalPrice(testOrder.getTotalPrice())
                    .status(newStatus)
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

            // when
            Order result = orderService.updateOrderStatus(orderId, newStatus);

            // then
            assertThat(result.getStatus()).isEqualTo(newStatus);
            assertThat(result.getUpdatedAt()).isNotNull();
            verify(orderRepository).findById(orderId);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("DELIVERED durumuna geçiş - deliveredAt set edilmeli")
        void whenUpdateOrderStatus_toDelivered_thenSetDeliveredAt() {
            // given
            Long orderId = 1L;
            OrderStatus newStatus = OrderStatus.DELIVERED;
            
            when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            Order result = orderService.updateOrderStatus(orderId, newStatus);

            // then
            assertThat(result.getStatus()).isEqualTo(newStatus);
            assertThat(result.getDeliveredAt()).isNotNull();
            verify(orderRepository).findById(orderId);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("Olmayan sipariş durumu güncelleme - hata")
        void whenUpdateOrderStatus_withNonExistentId_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            OrderStatus newStatus = OrderStatus.IN_PRODUCTION;
            when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(nonExistentId, newStatus))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Sipariş bulunamadı");

            verify(orderRepository).findById(nonExistentId);
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Fabrika Atama Testleri")
    class AssignFactoryTests {

        @Test
        @DisplayName("Fabrika atama - başarılı")
        void whenAssignFactory_thenReturnUpdatedOrder() {
            // given
            Long orderId = 1L;
            Long factoryId = 1L;
            LocalDate deadline = LocalDate.now().plusDays(30);
            
            BrandProcess brandProcess = BrandProcess.builder()
                    .brand(testBrand)
                    .status(ProcessStatus.SENT_TO_FACTORY)
                    .build();

            when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(testOrder));
            when(factoryService.getFactoryById(factoryId)).thenReturn(testFactory);
            when(brandProcessService.updateBrandProcessStatus(testBrand.getId(), ProcessStatus.SENT_TO_FACTORY))
                    .thenReturn(brandProcess);
            doNothing().when(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    any(BrandProcess.class), any(ProcessStatus.class), any(ProcessStatus.class), anyString());

            // when
            Order result = orderService.assignFactory(orderId, factoryId, deadline);

            // then
            assertThat(result.getFactory()).isEqualTo(testFactory);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PRODUCTION);
            assertThat(result.getUpdatedAt()).isNotNull();
            assertThat(result.getDeadline()).isEqualTo(deadline);

            verify(orderRepository).findByIdForUpdate(orderId);
            verify(factoryService).getFactoryById(factoryId);
            verify(brandProcessService).updateBrandProcessStatus(testBrand.getId(), ProcessStatus.SENT_TO_FACTORY);
            verify(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    eq(brandProcess), eq(ProcessStatus.SENT_TO_FACTORY), eq(ProcessStatus.ORDER_PLACED), anyString());
        }

        @Test
        @DisplayName("Fabrika atama - deadline olmadan")
        void whenAssignFactory_withoutDeadline_thenReturnUpdatedOrder() {
            // given
            Long orderId = 1L;
            Long factoryId = 1L;
            
            BrandProcess brandProcess = BrandProcess.builder()
                    .brand(testBrand)
                    .status(ProcessStatus.SENT_TO_FACTORY)
                    .build();

            when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(testOrder));
            when(factoryService.getFactoryById(factoryId)).thenReturn(testFactory);
            when(brandProcessService.updateBrandProcessStatus(testBrand.getId(), ProcessStatus.SENT_TO_FACTORY))
                    .thenReturn(brandProcess);
            doNothing().when(brandProcessHistoryService).saveProcessHistoryForChangeStatus(
                    any(BrandProcess.class), any(ProcessStatus.class), any(ProcessStatus.class), anyString());

            // when
            Order result = orderService.assignFactory(orderId, factoryId, null);

            // then
            assertThat(result.getFactory()).isEqualTo(testFactory);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PRODUCTION);
            assertThat(result.getUpdatedAt()).isNotNull();
            assertThat(result.getDeadline()).isNull();

            verify(orderRepository).findByIdForUpdate(orderId);
            verify(factoryService).getFactoryById(factoryId);
        }

        @Test
        @DisplayName("PENDING olmayan siparişe fabrika atama - hata")
        void whenAssignFactory_withNonPendingOrder_thenThrowException() {
            // given
            Long orderId = 1L;
            Long factoryId = 1L;
            
            Order inProductionOrder = Order.builder()
                    .id(orderId)
                    .quote(testQuote)
                    .status(OrderStatus.IN_PRODUCTION)
                    .build();

            when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(inProductionOrder));

            // when & then
            assertThatThrownBy(() -> orderService.assignFactory(orderId, factoryId, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Sadece PENDING sipariş atanabilir");

            verify(orderRepository).findByIdForUpdate(orderId);
            verify(factoryService, never()).getFactoryById(any());
        }

        @Test
        @DisplayName("Olmayan siparişe fabrika atama - hata")
        void whenAssignFactory_withNonExistentOrder_thenThrowException() {
            // given
            Long nonExistentId = 999L;
            Long factoryId = 1L;
            when(orderRepository.findByIdForUpdate(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.assignFactory(nonExistentId, factoryId, null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Order not found");

            verify(orderRepository).findByIdForUpdate(nonExistentId);
            verify(factoryService, never()).getFactoryById(any());
        }
    }
} 