package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteItemRequestDto;
import com.baskiliisler.backend.dto.QuoteUpdateDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.service.QuoteService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuoteControllerTest {

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private QuoteController quoteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(quoteController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Yeni teklif oluşturulduğunda")
    void whenCreateQuote_thenReturnCreated() throws Exception {
        // given
        QuoteItemRequestDto item = new QuoteItemRequestDto(
                1L,
                2,
                BigDecimal.valueOf(1000)
        );

        QuoteCreateDto request = new QuoteCreateDto(
                1L,
                List.of(item),
                LocalDate.now().plusDays(30)
        );

        Brand brand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .build();

        Quote createdQuote = Quote.builder()
                .id(1L)
                .brand(brand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(request.validUntil())
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(new ArrayList<>())
                .build();

        when(quoteService.createQuote(any(QuoteCreateDto.class))).thenReturn(createdQuote);

        // when & then
        mockMvc.perform(post("/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdQuote.getId()))
                .andExpect(jsonPath("$.status").value(createdQuote.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(createdQuote.getTotalPrice()));
    }

    @Test
    @DisplayName("Teklif güncellendiğinde")
    void whenUpdateQuote_thenReturnUpdated() throws Exception {
        // given
        Long quoteId = 1L;
        QuoteItemRequestDto updatedItem = new QuoteItemRequestDto(
                2L,
                3,
                BigDecimal.valueOf(1500)
        );

        QuoteUpdateDto request = new QuoteUpdateDto(
                List.of(updatedItem),
                LocalDate.now().plusDays(45)
        );

        Brand brand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .build();

        Quote updatedQuote = Quote.builder()
                .id(quoteId)
                .brand(brand)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(request.validUntil())
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(4500))
                .items(new ArrayList<>())
                .build();

        when(quoteService.updateQuote(any(Long.class), any(QuoteUpdateDto.class))).thenReturn(updatedQuote);

        // when & then
        mockMvc.perform(put("/quotes/{id}", quoteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedQuote.getId()))
                .andExpect(jsonPath("$.status").value(updatedQuote.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(updatedQuote.getTotalPrice()));
    }
} 