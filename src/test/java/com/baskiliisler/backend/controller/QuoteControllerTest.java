package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.config.GlobalExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuoteControllerTest {

    @Mock
    private QuoteService quoteService;

    @InjectMocks
    private QuoteController quoteController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Brand testBrand;
    private Quote testQuote;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(quoteController)
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
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2000))
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Yeni teklif oluşturulduğunda başarılı response döner")
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

        when(quoteService.createQuote(any(QuoteCreateDto.class))).thenReturn(testQuote);

        // when & then
        mockMvc.perform(post("/quotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testQuote.getId()))
                .andExpect(jsonPath("$.status").value(testQuote.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testQuote.getTotalPrice()));
    }

    @Test
    @DisplayName("Tüm teklifler listelendiğinde başarılı response döner")
    void whenGetAllQuotes_thenReturnQuoteList() throws Exception {
        // given
        List<Quote> quotes = List.of(testQuote);
        when(quoteService.getAllQuotes()).thenReturn(quotes);

        // when & then
        mockMvc.perform(get("/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testQuote.getId()))
                .andExpect(jsonPath("$[0].status").value(testQuote.getStatus().name()));
    }

    @Test
    @DisplayName("ID ile teklif getirildiğinde başarılı response döner")
    void whenGetQuoteById_thenReturnQuote() throws Exception {
        // given
        Long quoteId = 1L;
        when(quoteService.getQuoteById(quoteId)).thenReturn(testQuote);

        // when & then
        mockMvc.perform(get("/quotes/{id}", quoteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testQuote.getId()))
                .andExpect(jsonPath("$.status").value(testQuote.getStatus().name()))
                .andExpect(jsonPath("$.totalPrice").value(testQuote.getTotalPrice()));
    }

    @Test
    @DisplayName("Olmayan ID ile teklif getirilmeye çalışıldığında 404 döner")
    void whenGetQuoteByNonExistentId_thenReturn404() throws Exception {
        // given
        Long nonExistentId = 999L;
        when(quoteService.getQuoteById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Teklif bulunamadı"));

        // when & then
        mockMvc.perform(get("/quotes/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Markaya göre teklifler getirildiğinde başarılı response döner")
    void whenGetQuotesByBrand_thenReturnQuoteList() throws Exception {
        // given
        Long brandId = 1L;
        List<Quote> quotes = List.of(testQuote);
        when(quoteService.getQuotesByBrand(brandId)).thenReturn(quotes);

        // when & then
        mockMvc.perform(get("/quotes/brand/{brandId}", brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testQuote.getId()))
                .andExpect(jsonPath("$[0].status").value(testQuote.getStatus().name()));
    }

    @Test
    @DisplayName("Teklif güncellendiğinde başarılı response döner")
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

        Quote updatedQuote = Quote.builder()
                .id(quoteId)
                .brand(testBrand)
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

    @Test
    @DisplayName("Teklif silindiğinde 204 No Content döner")
    void whenDeleteQuote_thenReturnNoContent() throws Exception {
        // given
        Long quoteId = 1L;
        doNothing().when(quoteService).deleteQuote(quoteId);

        // when & then
        mockMvc.perform(delete("/quotes/{id}", quoteId))
                .andExpect(status().isNoContent());

        verify(quoteService).deleteQuote(quoteId);
    }

    @Test
    @DisplayName("Olmayan teklif silinmeye çalışıldığında 404 döner")
    void whenDeleteNonExistentQuote_thenReturn404() throws Exception {
        // given
        Long nonExistentId = 999L;
        doThrow(new EntityNotFoundException("Teklif bulunamadı"))
                .when(quoteService).deleteQuote(nonExistentId);

        // when & then
        mockMvc.perform(delete("/quotes/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Silinemez durumda teklif silinmeye çalışıldığında 400 döner")
    void whenDeleteUndeletableQuote_thenReturn400() throws Exception {
        // given
        Long quoteId = 1L;
        doThrow(new IllegalStateException("Bu teklif silinemez"))
                .when(quoteService).deleteQuote(quoteId);

        // when & then
        mockMvc.perform(delete("/quotes/{id}", quoteId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Teklif süresi dolduğunda 204 No Content döner")
    void whenExpireQuote_thenReturnNoContent() throws Exception {
        // given
        Long quoteId = 1L;
        doNothing().when(quoteService).expireQuote(quoteId);

        // when & then
        mockMvc.perform(patch("/quotes/{id}/expire", quoteId))
                .andExpect(status().isNoContent());

        verify(quoteService).expireQuote(quoteId);
    }

    @Test
    @DisplayName("Olmayan teklif expire edilmeye çalışıldığında 404 döner")
    void whenExpireNonExistentQuote_thenReturn404() throws Exception {
        // given
        Long nonExistentId = 999L;
        doThrow(new EntityNotFoundException("Teklif bulunamadı"))
                .when(quoteService).expireQuote(nonExistentId);

        // when & then
        mockMvc.perform(patch("/quotes/{id}/expire", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Expire edilemeyen teklif expire edilmeye çalışıldığında 400 döner")
    void whenExpireUnexpirableQuote_thenReturn400() throws Exception {
        // given
        Long quoteId = 1L;
        doThrow(new IllegalStateException("Teklif süresi dolmuş"))
                .when(quoteService).expireQuote(quoteId);

        // when & then
        mockMvc.perform(patch("/quotes/{id}/expire", quoteId))
                .andExpect(status().isBadRequest());
    }
} 