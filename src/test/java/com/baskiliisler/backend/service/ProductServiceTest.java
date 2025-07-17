package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.repository.ProductRepository;
import com.baskiliisler.backend.type.Unit;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequestDto testProductRequestDto;
    private ProductUpdateDto testProductUpdateDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Orta Karton Bardak")
                .description("Orta boy karton bardak açıklaması")
                .unit(Unit.ADET)
                .unitPrice(new BigDecimal("2.50"))
                .taxRate(new BigDecimal("18.00"))
                .active(true)
                .build();

        testProductRequestDto = new ProductRequestDto(
                "Orta Karton Bardak",
                "Orta boy karton bardak açıklaması",
                Unit.ADET,
                new BigDecimal("2.50"),
                new BigDecimal("18.00")
        );

        testProductUpdateDto = new ProductUpdateDto(
                "Büyük Karton Bardak",
                "Büyük boy karton bardak açıklaması",
                Unit.ADET,
                new BigDecimal("3.50"),
                new BigDecimal("18.00"),
                true
        );
    }

    @Nested
    @DisplayName("Ürün oluşturma")
    class CreateProduct {

        @Test
        @DisplayName("Geçerli verilerle ürün oluşturulduğunda başarılı olmalı")
        void givenValidProductData_whenCreateProduct_thenShouldReturnCreatedProduct() {
            // Given
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            Product result = productService.createProduct(testProductRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(testProductRequestDto.name());
            assertThat(result.getDescription()).isEqualTo(testProductRequestDto.description());
            assertThat(result.getUnit()).isEqualTo(testProductRequestDto.unit());
            assertThat(result.getUnitPrice()).isEqualTo(testProductRequestDto.unitPrice());
            assertThat(result.getTaxRate()).isEqualTo(testProductRequestDto.taxRate());
            assertThat(result.isActive()).isTrue();

            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Ürün listeleme")
    class ListProducts {

        @Test
        @DisplayName("Tüm ürünler listelendiğinde başarılı olmalı")
        void whenGetAllProducts_thenShouldReturnAllProducts() {
            // Given
            when(productRepository.findAll()).thenReturn(List.of(testProduct));

            // When
            List<Product> result = productService.getAllProducts();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo(testProduct.getName());

            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Aktif ürünler listelendiğinde başarılı olmalı")
        void whenGetActiveProducts_thenShouldReturnActiveProducts() {
            // Given
            when(productRepository.findByActiveTrue()).thenReturn(List.of(testProduct));

            // When
            List<Product> result = productService.getActiveProducts();

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isActive()).isTrue();

            verify(productRepository).findByActiveTrue();
        }
    }

    @Nested
    @DisplayName("Ürün arama")
    class FindProduct {

        @Test
        @DisplayName("Geçerli ID ile ürün arandığında başarılı olmalı")
        void givenValidId_whenFindById_thenShouldReturnProduct() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // When
            ProductResponseDto result = productService.findById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testProduct.getId());
            assertThat(result.name()).isEqualTo(testProduct.getName());
            assertThat(result.description()).isEqualTo(testProduct.getDescription());
            assertThat(result.unit()).isEqualTo(testProduct.getUnit());
            assertThat(result.taxRate()).isEqualTo(testProduct.getTaxRate());

            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Geçersiz ID ile ürün arandığında hata fırlatmalı")
        void givenInvalidId_whenFindById_thenShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.findById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Ürün güncelleme")
    class UpdateProduct {

        @Test
        @DisplayName("Geçerli verilerle ürün güncellendiğinde başarılı olmalı")
        void givenValidUpdateData_whenUpdateProduct_thenShouldReturnUpdatedProduct() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponseDto result = productService.updateProduct(1L, testProductUpdateDto);

            // Then
            assertThat(result).isNotNull();

            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Geçersiz ID ile güncelleme yapıldığında hata fırlatmalı")
        void givenInvalidId_whenUpdateProduct_thenShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(999L, testProductUpdateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Ürün silme")
    class DeleteProduct {

        @Test
        @DisplayName("Mevcut ürün silindiğinde başarılı olmalı")
        void givenExistingProductId_whenDeleteProduct_thenShouldDeleteProduct() {
            // Given
            when(productRepository.existsById(1L)).thenReturn(true);
            doNothing().when(productRepository).deleteById(1L);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository).existsById(1L);
            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün silinmeye çalışıldığında hata fırlatmalı")
        void givenNonExistingProductId_whenDeleteProduct_thenShouldThrowException() {
            // Given
            when(productRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).existsById(999L);
            verify(productRepository, never()).deleteById(999L);
        }
    }

    @Nested
    @DisplayName("Ürün aktiflik durumu")
    class ProductActivation {

        @Test
        @DisplayName("Ürün aktifleştirildiğinde başarılı olmalı")
        void givenExistingProduct_whenActivateProduct_thenShouldActivateProduct() {
            // Given
            testProduct.setActive(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.activateProduct(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Ürün pasifleştirildiğinde başarılı olmalı")
        void givenExistingProduct_whenDeactivateProduct_thenShouldDeactivateProduct() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.deactivateProduct(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut olmayan ürün aktifleştirilmeye çalışıldığında hata fırlatmalı")
        void givenNonExistingProduct_whenActivateProduct_thenShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.activateProduct(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }
} 