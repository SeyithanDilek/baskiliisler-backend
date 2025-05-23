package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.QuoteItemRequestDto;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.QuoteItem;
import com.baskiliisler.backend.repository.ProductRepository;
import com.baskiliisler.backend.repository.QuoteItemRepository;
import com.baskiliisler.backend.type.QuoteStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteItemServiceTest {

    @Mock
    private QuoteItemRepository quoteItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private QuoteItemService quoteItemService;

    private Quote testQuote;
    private Product testProduct;
    private QuoteItemRequestDto testItemRequest;
    private QuoteItem testQuoteItem;

    @BeforeEach
    void setUp() {
        Brand testBrand = Brand.builder()
                .id(1L)
                .name("Test Brand")
                .build();

        testQuote = Quote.builder()
                .id(1L)
                .brand(testBrand)
                .status(QuoteStatus.DRAFT)
                .validUntil(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.ZERO)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .code("TEST001")
                .build();

        testItemRequest = new QuoteItemRequestDto(
                1L,
                5,
                BigDecimal.valueOf(100)
        );

        testQuoteItem = QuoteItem.builder()
                .id(1L)
                .quote(testQuote)
                .product(testProduct)
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(100))
                .lineTotal(BigDecimal.valueOf(500))
                .build();
    }

    @Nested
    @DisplayName("Quote Item Assembly Testleri")
    class AssembleQuoteItemsTests {

        @Test
        @DisplayName("Başarılı quote item oluşturma ve toplam hesaplama")
        void whenAssembleQuoteItems_thenCreateItemsAndReturnTotal() {
            // given
            QuoteItemRequestDto item1 = new QuoteItemRequestDto(1L, 2, BigDecimal.valueOf(100));
            QuoteItemRequestDto item2 = new QuoteItemRequestDto(2L, 3, BigDecimal.valueOf(200));
            List<QuoteItemRequestDto> itemRequests = List.of(item1, item2);

            Product product1 = Product.builder().id(1L).name("Product 1").build();
            Product product2 = Product.builder().id(2L).name("Product 2").build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
            when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
            when(quoteItemRepository.save(any(QuoteItem.class))).thenAnswer(i -> i.getArgument(0));

            // when
            BigDecimal total = quoteItemService.assembleAndSaveQuoteItems(testQuote, itemRequests);

            // then
            assertThat(total).isEqualTo(BigDecimal.valueOf(800)); // (2*100) + (3*200) = 800

            ArgumentCaptor<QuoteItem> quoteItemCaptor = ArgumentCaptor.forClass(QuoteItem.class);
            verify(quoteItemRepository, times(2)).save(quoteItemCaptor.capture());

            List<QuoteItem> savedItems = quoteItemCaptor.getAllValues();
            
            // First item verification
            QuoteItem savedItem1 = savedItems.get(0);
            assertThat(savedItem1.getQuote()).isEqualTo(testQuote);
            assertThat(savedItem1.getProduct()).isEqualTo(product1);
            assertThat(savedItem1.getQuantity()).isEqualTo(2);
            assertThat(savedItem1.getUnitPrice()).isEqualTo(BigDecimal.valueOf(100));
            assertThat(savedItem1.getLineTotal()).isEqualTo(BigDecimal.valueOf(200));

            // Second item verification
            QuoteItem savedItem2 = savedItems.get(1);
            assertThat(savedItem2.getQuote()).isEqualTo(testQuote);
            assertThat(savedItem2.getProduct()).isEqualTo(product2);
            assertThat(savedItem2.getQuantity()).isEqualTo(3);
            assertThat(savedItem2.getUnitPrice()).isEqualTo(BigDecimal.valueOf(200));
            assertThat(savedItem2.getLineTotal()).isEqualTo(BigDecimal.valueOf(600));
        }

        @Test
        @DisplayName("Olmayan ürün ile quote item oluşturma")
        void whenAssembleQuoteItems_withNonExistingProduct_thenThrowException() {
            // given
            List<QuoteItemRequestDto> itemRequests = List.of(testItemRequest);
            when(productRepository.findById(testItemRequest.productId())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> quoteItemService.assembleAndSaveQuoteItems(testQuote, itemRequests))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün yok");

            verify(productRepository).findById(testItemRequest.productId());
            verify(quoteItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Boş item listesi ile toplam hesaplama")
        void whenAssembleQuoteItems_withEmptyList_thenReturnZero() {
            // given
            List<QuoteItemRequestDto> emptyList = List.of();

            // when
            BigDecimal total = quoteItemService.assembleAndSaveQuoteItems(testQuote, emptyList);

            // then
            assertThat(total).isEqualTo(BigDecimal.ZERO);
            verify(productRepository, never()).findById(any());
            verify(quoteItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Sıfır fiyatlı ürün ile toplam hesaplama")
        void whenAssembleQuoteItems_withZeroPrice_thenCalculateCorrectly() {
            // given
            QuoteItemRequestDto zeroPrice = new QuoteItemRequestDto(1L, 5, BigDecimal.ZERO);
            List<QuoteItemRequestDto> itemRequests = List.of(zeroPrice);

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(quoteItemRepository.save(any(QuoteItem.class))).thenAnswer(i -> i.getArgument(0));

            // when
            BigDecimal total = quoteItemService.assembleAndSaveQuoteItems(testQuote, itemRequests);

            // then
            assertThat(total).isEqualTo(BigDecimal.ZERO);
            
            ArgumentCaptor<QuoteItem> quoteItemCaptor = ArgumentCaptor.forClass(QuoteItem.class);
            verify(quoteItemRepository).save(quoteItemCaptor.capture());
            
            QuoteItem savedItem = quoteItemCaptor.getValue();
            assertThat(savedItem.getLineTotal()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Quote Item Silme Testleri")
    class DeleteQuoteItemsTests {

        @Test
        @DisplayName("Quote item'ları başarıyla silme")
        void whenDeleteQuoteItems_thenDeleteAll() {
            // given
            List<QuoteItem> itemsToDelete = List.of(testQuoteItem);
            doNothing().when(quoteItemRepository).deleteAll(itemsToDelete);

            // when
            quoteItemService.deleteQuoteItems(itemsToDelete);

            // then
            verify(quoteItemRepository).deleteAll(itemsToDelete);
        }

        @Test
        @DisplayName("Boş liste ile silme işlemi")
        void whenDeleteQuoteItems_withEmptyList_thenCallDeleteAll() {
            // given
            List<QuoteItem> emptyList = List.of();
            doNothing().when(quoteItemRepository).deleteAll(emptyList);

            // when
            quoteItemService.deleteQuoteItems(emptyList);

            // then
            verify(quoteItemRepository).deleteAll(emptyList);
        }

        @Test
        @DisplayName("Null liste ile silme işlemi")
        void whenDeleteQuoteItems_withNullList_thenCallDeleteAll() {
            // given
            List<QuoteItem> nullList = null;
            doNothing().when(quoteItemRepository).deleteAll(nullList);

            // when
            quoteItemService.deleteQuoteItems(nullList);

            // then
            verify(quoteItemRepository).deleteAll(nullList);
        }
    }
} 