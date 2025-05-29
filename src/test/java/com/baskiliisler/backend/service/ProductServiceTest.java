package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.repository.ProductRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
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
                .code("PAP_CUP_M")
                .name("Orta Karton Bardak")
                .unit("adet")
                .unitPrice(new BigDecimal("2.50"))
                .active(true)
                .build();

        testProductRequestDto = new ProductRequestDto(
                "PAP_CUP_M",
                "Orta Karton Bardak",
                "adet",
                new BigDecimal("2.50")
        );

        testProductUpdateDto = new ProductUpdateDto(
                "PAP_CUP_L",
                "Büyük Karton Bardak",
                "adet",
                new BigDecimal("3.50"),
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
            when(productRepository.existsByCode(testProductRequestDto.code())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            Product result = productService.createProduct(testProductRequestDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(testProductRequestDto.code());
            assertThat(result.getName()).isEqualTo(testProductRequestDto.name());
            assertThat(result.getUnit()).isEqualTo(testProductRequestDto.unit());
            assertThat(result.getUnitPrice()).isEqualTo(testProductRequestDto.unitPrice());
            assertThat(result.isActive()).isTrue();

            verify(productRepository).existsByCode(testProductRequestDto.code());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut kod ile ürün oluşturulmaya çalışıldığında hata fırlatmalı")
        void givenExistingProductCode_whenCreateProduct_thenShouldThrowException() {
            // Given
            when(productRepository.existsByCode(testProductRequestDto.code())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.createProduct(testProductRequestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bu kod ile bir ürün zaten mevcut");

            verify(productRepository).existsByCode(testProductRequestDto.code());
            verify(productRepository, never()).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Ürün listeleme")
    class ListProducts {

        @Test
        @DisplayName("Tüm ürünler listelendiğinde başarılı olmalı")
        void whenGetAllProducts_thenShouldReturnAllProducts() {
            // Given
            List<Product> products = List.of(testProduct);
            when(productRepository.findAll()).thenReturn(products);

            // When
            List<Product> result = productService.getAllProducts();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testProduct);

            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Aktif ürünler listelendiğinde başarılı olmalı")
        void whenGetActiveProducts_thenShouldReturnActiveProducts() {
            // Given
            List<Product> activeProducts = List.of(testProduct);
            when(productRepository.findByActiveTrue()).thenReturn(activeProducts);

            // When
            List<Product> result = productService.getActiveProducts();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testProduct);

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
            assertThat(result.code()).isEqualTo(testProduct.getCode());
            assertThat(result.name()).isEqualTo(testProduct.getName());

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

        @Test
        @DisplayName("Geçerli kod ile ürün arandığında başarılı olmalı")
        void givenValidCode_whenFindByCode_thenShouldReturnProduct() {
            // Given
            when(productRepository.findByCode("PAP_CUP_M")).thenReturn(Optional.of(testProduct));

            // When
            ProductResponseDto result = productService.findByCode("PAP_CUP_M");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo(testProduct.getCode());

            verify(productRepository).findByCode("PAP_CUP_M");
        }

        @Test
        @DisplayName("Geçersiz kod ile ürün arandığında hata fırlatmalı")
        void givenInvalidCode_whenFindByCode_thenShouldThrowException() {
            // Given
            when(productRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.findByCode("INVALID_CODE"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: INVALID_CODE");

            verify(productRepository).findByCode("INVALID_CODE");
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
            when(productRepository.existsByCode(testProductUpdateDto.code())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponseDto result = productService.updateProduct(1L, testProductUpdateDto);

            // Then
            assertThat(result).isNotNull();

            verify(productRepository).findById(1L);
            verify(productRepository).existsByCode(testProductUpdateDto.code());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut kod ile güncelleme yapıldığında hata fırlatmalı")
        void givenExistingCode_whenUpdateProduct_thenShouldThrowException() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByCode(testProductUpdateDto.code())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.updateProduct(1L, testProductUpdateDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bu kod ile bir ürün zaten mevcut");

            verify(productRepository).findById(1L);
            verify(productRepository).existsByCode(testProductUpdateDto.code());
            verify(productRepository, never()).save(any(Product.class));
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
        void givenExistingProduct_whenDeleteProduct_thenShouldDeleteSuccessfully() {
            // Given
            when(productRepository.existsById(1L)).thenReturn(true);

            // When
            productService.deleteProduct(1L);

            // Then
            verify(productRepository).existsById(1L);
            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Mevcut olmayan ürün silinmeye çalışıldığında hata fırlatmalı")
        void givenNonExistingProduct_whenDeleteProduct_thenShouldThrowException() {
            // Given
            when(productRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).existsById(999L);
            verify(productRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Ürün aktiflik durumu")
    class ProductActivation {

        @Test
        @DisplayName("Ürün pasifleştirildiğinde başarılı olmalı")
        void givenActiveProduct_whenDeactivateProduct_thenShouldDeactivateSuccessfully() {
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
        @DisplayName("Ürün aktifleştirildiğinde başarılı olmalı")
        void givenInactiveProduct_whenActivateProduct_thenShouldActivateSuccessfully() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.activateProduct(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Mevcut olmayan ürün pasifleştirilmeye çalışıldığında hata fırlatmalı")
        void givenNonExistingProduct_whenDeactivateProduct_thenShouldThrowException() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.deactivateProduct(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Ürün bulunamadı: 999");

            verify(productRepository).findById(999L);
            verify(productRepository, never()).save(any(Product.class));
        }
    }
} 