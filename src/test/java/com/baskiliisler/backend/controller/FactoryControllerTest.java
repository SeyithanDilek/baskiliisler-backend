package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.FactoryAssignDto;
import com.baskiliisler.backend.dto.FactoryRequestDto;
import com.baskiliisler.backend.dto.FactoryResponseDto;
import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.service.FactoryService;
import com.baskiliisler.backend.service.OrderService;
import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("FactoryController Test")
class FactoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private FactoryService factoryService;

    @InjectMocks
    private FactoryController factoryController;

    private ObjectMapper objectMapper;

    private Order testOrder;
    private Factory testFactory;
    private FactoryAssignDto assignDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(factoryController)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();

        Quote testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.ACCEPTED)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.valueOf(2500))
                .items(new ArrayList<>())
                .build();

        testFactory = Factory.builder()
                .id(1L)
                .name("Test Factory")
                .address("Test Address")
                .phoneNumber("+90 555 123 45 67")
                .active(true)
                .build();

        Product testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .code("TEST_PROD")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(10)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(1000))
                .plannedDelivery(LocalDate.now().plusDays(14))
                .status(OrderItemStatus.PENDING)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .quote(testQuote)
                .factory(testFactory)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .deadline(LocalDate.now().plusDays(30))
                .totalPrice(BigDecimal.valueOf(2500))
                .status(OrderStatus.IN_PRODUCTION)
                .build();

        testOrder.getItems().add(orderItem);
        orderItem.setOrder(testOrder);

        assignDto = new FactoryAssignDto(1L, LocalDate.now().plusDays(30));
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
        public ResponseEntity<String> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @Test
    @DisplayName("Siparişe fabrika ataması başarılı")
    void whenAssignFactory_thenReturnOrderResponse() throws Exception {
        // given
        when(orderService.assignFactory(eq(1L), eq(1L), any(LocalDate.class)))
                .thenReturn(testOrder);

        // when & then
        mockMvc.perform(patch("/factories/orders/1/assign-factory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("IN_PRODUCTION"))
                .andExpect(jsonPath("$.totalPrice").value(2500))
                .andExpect(jsonPath("$.factory.id").value(1L))
                .andExpect(jsonPath("$.factory.name").value("Test Factory"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].productId").value(1L))
                .andExpect(jsonPath("$.items[0].quantity").value(10));
    }

    @Test
    @DisplayName("Geçersiz JSON ile fabrika ataması")
    void whenAssignFactory_withInvalidJson_thenReturnBadRequest() throws Exception {
        // given
        String invalidJson = "{ invalid json }";

        // when & then
        mockMvc.perform(patch("/factories/orders/1/assign-factory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Null factoryId ile fabrika ataması")
    void whenAssignFactory_withNullFactoryId_thenReturnBadRequest() throws Exception {
        // given
        FactoryAssignDto invalidDto = new FactoryAssignDto(null, LocalDate.now().plusDays(30));

        // when & then
        mockMvc.perform(patch("/factories/orders/1/assign-factory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Olmayan sipariş ID ile fabrika ataması")
    void whenAssignFactory_withNonExistingOrderId_thenReturnNotFound() throws Exception {
        // given
        when(orderService.assignFactory(eq(999L), eq(1L), any(LocalDate.class)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Order not found"));

        // when & then
        mockMvc.perform(patch("/factories/orders/999/assign-factory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Geçersiz sipariş durumu ile fabrika ataması")
    void whenAssignFactory_withInvalidOrderStatus_thenReturnBadRequest() throws Exception {
        // given
        LocalDate deadline = LocalDate.now().plusDays(30);
        FactoryAssignDto invalidStatusDto = new FactoryAssignDto(2L, deadline);
        
        when(orderService.assignFactory(eq(1L), eq(2L), eq(deadline)))
                .thenThrow(new IllegalStateException("Sadece PENDING sipariş atanabilir"));

        // when & then
        mockMvc.perform(patch("/factories/orders/1/assign-factory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStatusDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Yeni fabrika oluşturma")
    void whenCreateFactory_thenReturnCreatedFactory() throws Exception {
        // given
        FactoryRequestDto requestDto = new FactoryRequestDto(
                "New Factory",
                "New Address",
                "+90 555 999 88 77",
                true
        );

        Factory createdFactory = Factory.builder()
                .id(1L)
                .name("New Factory")
                .address("New Address")

                .active(true)
                .build();

        when(factoryService.create(any(FactoryRequestDto.class))).thenReturn(createdFactory);

        // when & then
        mockMvc.perform(post("/factories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Factory"))
                .andExpect(jsonPath("$.address").value("New Address"))

                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Geçersiz isim ile fabrika oluşturma")
    void whenCreateFactory_withInvalidName_thenReturnBadRequest() throws Exception {
        // given
        FactoryRequestDto requestDto = new FactoryRequestDto(
                "",  // boş isim
                "Address",
                "+90 555 111 22 33",
                true
        );

        // when & then
        mockMvc.perform(post("/factories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Fabrika güncelleme")
    void whenUpdateFactory_thenReturnUpdatedFactory() throws Exception {
        // given
        Long factoryId = 1L;
        FactoryRequestDto requestDto = new FactoryRequestDto(
                "Updated Factory",
                "Updated Address",
                "+90 555 777 66 55",
                false
        );

        Factory updatedFactory = Factory.builder()
                .id(factoryId)
                .name("Updated Factory")
                .address("Updated Address")

                .active(false)
                .build();

        when(factoryService.update(eq(factoryId), any(FactoryRequestDto.class))).thenReturn(updatedFactory);

        // when & then
        mockMvc.perform(patch("/factories/{id}", factoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Factory"))
                .andExpect(jsonPath("$.address").value("Updated Address"))

                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Olmayan fabrikayı güncelleme")
    void whenUpdateFactory_withNonExistingId_thenReturnNotFound() throws Exception {
        // given
        Long nonExistingId = 999L;
        FactoryRequestDto requestDto = new FactoryRequestDto(
                "Non Existing Factory",
                "Address",
                "+90 555 888 99 00",
                true
        );

        when(factoryService.update(eq(nonExistingId), any(FactoryRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Factory not found"));

        // when & then
        mockMvc.perform(patch("/factories/{id}", nonExistingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tüm fabrikaları listeleme")
    void whenListFactories_withOnlyActiveFalse_thenReturnAllFactories() throws Exception {
        // given
        List<FactoryResponseDto> factories = List.of(
                new FactoryResponseDto(1L, "Factory 1", "Address 1", "+90 555 111 11 11", true),
                new FactoryResponseDto(2L, "Factory 2", "Address 2", "+90 555 222 22 22", false)
        );

        when(factoryService.list(false)).thenReturn(factories);

        // when & then
        mockMvc.perform(get("/factories")
                        .param("onlyActive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Factory 1"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Factory 2"))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    @DisplayName("Sadece aktif fabrikaları listeleme")
    void whenListFactories_withOnlyActiveTrue_thenReturnOnlyActiveFactories() throws Exception {
        // given
        List<FactoryResponseDto> activeFactories = List.of(
                new FactoryResponseDto(1L, "Active Factory", "Address", "+90 555 333 33 33", true)
        );

        when(factoryService.list(true)).thenReturn(activeFactories);

        // when & then
        mockMvc.perform(get("/factories")
                        .param("onlyActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Active Factory"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("Varsayılan parametrelerle fabrika listeleme")
    void whenListFactories_withDefaultParams_thenReturnAllFactories() throws Exception {
        // given
        List<FactoryResponseDto> factories = List.of(
                new FactoryResponseDto(1L, "Factory 1", "Address 1", "+90 555 444 44 44", true)
        );

        when(factoryService.list(false)).thenReturn(factories); // default false

        // when & then
        mockMvc.perform(get("/factories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(factoryService).list(false); // default değer
    }

    @Test
    @DisplayName("ID ile fabrika getirme")
    void whenGetFactory_withExistingId_thenReturnFactory() throws Exception {
        // given
        Long factoryId = 1L;
        FactoryResponseDto factory = new FactoryResponseDto(
                factoryId, "Test Factory", "Test Address", "+90 555 555 55 55", true
        );

        when(factoryService.get(factoryId)).thenReturn(factory);

        // when & then
        mockMvc.perform(get("/factories/{id}", factoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Factory"))
                .andExpect(jsonPath("$.address").value("Test Address"))

                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Olmayan ID ile fabrika getirme")
    void whenGetFactory_withNonExistingId_thenReturnNotFound() throws Exception {
        // given
        Long nonExistingId = 999L;
        when(factoryService.get(nonExistingId))
                .thenThrow(new IllegalArgumentException("Factory not found"));

        // when & then
        mockMvc.perform(get("/factories/{id}", nonExistingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Fabrika silme")
    void whenDeleteFactory_thenReturnNoContent() throws Exception {
        // given
        Long factoryId = 1L;

        // when & then
        mockMvc.perform(delete("/factories/{id}", factoryId))
                .andExpect(status().isNoContent());

        verify(factoryService).delete(factoryId);
    }
} 