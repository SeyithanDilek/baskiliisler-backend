package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.dto.QuoteAcceptDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Quote testQuote = Quote.builder()
                .id(quoteId)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(1))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2500))
                .items(new ArrayList<>())
                .build();

        Order createdOrder = Order.builder()
                .id(1L)
                .quote(testQuote)
                .createdAt(LocalDateTime.now())
                .totalPrice(testQuote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        when(quoteService.acceptQuote(eq(quoteId), eq(itemDeadlines))).thenReturn(createdOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(jsonPath("$.status").value(createdOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(createdOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Teklif kabul edildiğinde sipariş oluşturma - teslim tarihleri olmadan")
    void whenAcceptQuote_withoutDeadlines_thenReturnCreatedOrder() throws Exception {
        // given
        Long quoteId = 2L;
        QuoteAcceptDto request = new QuoteAcceptDto(null);

        Brand testBrand = Brand.builder()
                .id(2L)
                .name("Another Test Brand")
                .contactEmail("another@brand.com")
                .contactPhone("0987654321")
                .build();

        Quote testQuote = Quote.builder()
                .id(quoteId)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(45))
                .createdAt(LocalDateTime.now().minusDays(2))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(1800))
                .items(new ArrayList<>())
                .build();

        Order createdOrder = Order.builder()
                .id(2L)
                .quote(testQuote)
                .createdAt(LocalDateTime.now())
                .totalPrice(testQuote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        when(quoteService.acceptQuote(eq(quoteId), any())).thenReturn(createdOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(jsonPath("$.status").value(createdOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(createdOrder.getTotalPrice()));
    }

    @Test
    @DisplayName("Teklif kabul edildiğinde sipariş oluşturma - boş teslim tarihleri haritası")
    void whenAcceptQuote_withEmptyDeadlines_thenReturnCreatedOrder() throws Exception {
        // given
        Long quoteId = 3L;
        Map<Long, LocalDate> emptyDeadlines = Map.of();
        QuoteAcceptDto request = new QuoteAcceptDto(emptyDeadlines);

        Brand testBrand = Brand.builder()
                .id(3L)
                .name("Third Test Brand")
                .contactEmail("third@brand.com")
                .contactPhone("1122334455")
                .build();

        Quote testQuote = Quote.builder()
                .id(quoteId)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(60))
                .createdAt(LocalDateTime.now().minusDays(3))
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(3200))
                .items(new ArrayList<>())
                .build();

        Order createdOrder = Order.builder()
                .id(3L)
                .quote(testQuote)
                .createdAt(LocalDateTime.now())
                .totalPrice(testQuote.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        when(quoteService.acceptQuote(eq(quoteId), eq(emptyDeadlines))).thenReturn(createdOrder);

        // when & then
        mockMvc.perform(patch("/orders/{id}/accept", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(jsonPath("$.status").value(createdOrder.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(createdOrder.getTotalPrice()));
    }
} 