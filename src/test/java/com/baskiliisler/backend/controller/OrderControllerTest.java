package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.config.GlobalExceptionHandler;
import com.baskiliisler.backend.dto.FactoryAssignDto;
import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.dto.OrderStatusUpdateDto;
import com.baskiliisler.backend.dto.QuoteAcceptDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.service.OrderService;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private QuoteService quoteService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Brand testBrand;
    private Quote testQuote;
    private Order testOrder;
    private Factory testFactory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(1))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2500))
                .items(new ArrayList<>())
                .build();

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

                .active(true)
                .build();
    }

    @Test
    @DisplayName("Tüm siparişler listelendiğinde başarılı response döner")
    void whenGetAllOrders_thenReturnOrderList() throws Exception {
        // given
        List<Order> orders = List.of(testOrder);
        when(orderService.getAllOrders()).thenReturn(orders);

        // when & then
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$[0].status").value(testOrder.getStatus().name()));
    }

    @Test
    @DisplayName("ID ile sipariş getirildiğinde başarılı response döner")
    void whenGetOrderById_thenReturnOrder() throws Exception {
        // given
        Long orderId = 1L;
        when(orderService.getOrderById(orderId)).thenReturn(testOrder);

        // when & then
        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value(testOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Olmayan ID ile sipariş getirilmeye çalışıldığında 404 döner")
    void whenGetOrderByNonExistentId_thenReturn404() throws Exception {
        // given
        Long nonExistentId = 999L;
        when(orderService.getOrderById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Sipariş bulunamadı"));

        // when & then
        mockMvc.perform(get("/orders/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Markaya göre siparişler getirildiğinde başarılı response döner")
    void whenGetOrdersByBrand_thenReturnOrderList() throws Exception {
        // given
        Long brandId = 1L;
        List<Order> orders = List.of(testOrder);
        when(orderService.getOrdersByBrand(brandId)).thenReturn(orders);

        // when & then
        mockMvc.perform(get("/orders/brand/{brandId}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$[0].status").value(testOrder.getStatus().name()));
    }

    @Test
    @DisplayName("Sipariş durumu güncellendiğinde başarılı response döner")
    void whenUpdateOrderStatus_thenReturnUpdatedOrder() throws Exception {
        // given
        Long orderId = 1L;
        OrderStatusUpdateDto request = new OrderStatusUpdateDto(OrderStatus.IN_PRODUCTION);
        
        Order updatedOrder = Order.builder()
                .id(orderId)
                .quote(testQuote)
                .createdAt(testOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .totalPrice(testOrder.getTotalPrice())
                .status(OrderStatus.IN_PRODUCTION)
                .build();

        when(orderService.updateOrderStatus(orderId, request.status())).thenReturn(updatedOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedOrder.getId()))
                .andExpect(jsonPath("$.status").value(updatedOrder.getStatus().name()));
    }

    @Test
    @DisplayName("Geçersiz durumla sipariş güncellenmeye çalışıldığında 400 döner")
    void whenUpdateOrderWithInvalidStatus_thenReturn400() throws Exception {
        // given
        Long orderId = 1L;
        OrderStatusUpdateDto request = new OrderStatusUpdateDto(OrderStatus.DELIVERED);
        
        when(orderService.updateOrderStatus(orderId, request.status()))
                .thenThrow(new IllegalStateException("Geçersiz durum geçişi"));

        // when & then
        mockMvc.perform(patch("/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Siparişe fabrika atandığında başarılı response döner")
    void whenAssignFactory_thenReturnUpdatedOrder() throws Exception {
        // given
        Long orderId = 1L;
        FactoryAssignDto request = new FactoryAssignDto(1L, LocalDate.now().plusDays(30));
        
        Order updatedOrder = Order.builder()
                .id(orderId)
                .quote(testQuote)
                .factory(testFactory)
                .createdAt(testOrder.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .totalPrice(testOrder.getTotalPrice())
                .status(OrderStatus.IN_PRODUCTION)
                .build();

        when(orderService.assignFactory(orderId, request.factoryId(), request.deadline())).thenReturn(updatedOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/assign-factory", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedOrder.getId()))
                .andExpect(jsonPath("$.factory.id").value(testFactory.getId()));
    }

    @Test
    @DisplayName("Geçersiz fabrika ile atama yapılmaya çalışıldığında 400 döner")
    void whenAssignInvalidFactory_thenReturn400() throws Exception {
        // given
        Long orderId = 1L;
        FactoryAssignDto request = new FactoryAssignDto(999L, null);
        
        when(orderService.assignFactory(orderId, request.factoryId(), request.deadline()))
                .thenThrow(new EntityNotFoundException("Fabrika bulunamadı"));

        // when & then
        mockMvc.perform(patch("/orders/{id}/assign-factory", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Teklif kabul edildiğinde sipariş oluşturma - teslim tarihleri ile")
    void whenAcceptQuote_withDeadlines_thenReturnCreatedOrder() throws Exception {
        // given
        Long quoteId = 1L;
        Map<Long, LocalDate> itemDeadlines = Map.of(
                1L, LocalDate.now().plusDays(14),
                2L, LocalDate.now().plusDays(21)
        );
        
        QuoteAcceptDto request = new QuoteAcceptDto(itemDeadlines);

        when(quoteService.acceptQuote(eq(quoteId), eq(itemDeadlines))).thenReturn(testOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value(testOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Teklif kabul edildiğinde sipariş oluşturma - teslim tarihleri olmadan")
    void whenAcceptQuote_withoutDeadlines_thenReturnCreatedOrder() throws Exception {
        // given
        Long quoteId = 2L;
        QuoteAcceptDto request = new QuoteAcceptDto(null);

        when(quoteService.acceptQuote(eq(quoteId), any())).thenReturn(testOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value(testOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Teklif kabul edildiğinde sipariş oluşturma - boş teslim tarihleri haritası")
    void whenAcceptQuote_withEmptyDeadlines_thenReturnCreatedOrder() throws Exception {
        // given
        Long quoteId = 3L;
        Map<Long, LocalDate> emptyDeadlines = Map.of();
        QuoteAcceptDto request = new QuoteAcceptDto(emptyDeadlines);

        when(quoteService.acceptQuote(eq(quoteId), eq(emptyDeadlines))).thenReturn(testOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.status").value(testOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Kabul edilemeyen teklif kabul edilmeye çalışıldığında 400 döner")
    void whenAcceptUnacceptableQuote_thenReturn400() throws Exception {
        // given
        Long quoteId = 1L;
        QuoteAcceptDto request = new QuoteAcceptDto(null);
        
        when(quoteService.acceptQuote(eq(quoteId), any()))
                .thenThrow(new IllegalStateException("Teklif kabul edilemez"));

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
} 