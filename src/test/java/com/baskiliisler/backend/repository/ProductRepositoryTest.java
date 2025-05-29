package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ProductRepository")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product activeProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        activeProduct = Product.builder()
                .code("PAP_CUP_M")
                .name("Orta Karton Bardak")
                .unit("adet")
                .unitPrice(new BigDecimal("2.50"))
                .active(true)
                .build();

        inactiveProduct = Product.builder()
                .code("PAP_CUP_L")
                .name("Büyük Karton Bardak")
                .unit("adet")
                .unitPrice(new BigDecimal("3.50"))
                .active(false)
                .build();

        entityManager.persistAndFlush(activeProduct);
        entityManager.persistAndFlush(inactiveProduct);
    }

    @Nested
    @DisplayName("Kod ile arama")
    class FindByCode {

        @Test
        @DisplayName("Mevcut kod ile arama yapıldığında ürün döndürmeli")
        void givenExistingCode_whenFindByCode_thenShouldReturnProduct() {
            // When
            Optional<Product> result = productRepository.findByCode("PAP_CUP_M");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("PAP_CUP_M");
            assertThat(result.get().getName()).isEqualTo("Orta Karton Bardak");
        }

        @Test
        @DisplayName("Mevcut olmayan kod ile arama yapıldığında boş döndürmeli")
        void givenNonExistingCode_whenFindByCode_thenShouldReturnEmpty() {
            // When
            Optional<Product> result = productRepository.findByCode("NON_EXISTING");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Null kod ile arama yapıldığında boş döndürmeli")
        void givenNullCode_whenFindByCode_thenShouldReturnEmpty() {
            // When
            Optional<Product> result = productRepository.findByCode(null);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Kod varlık kontrolü")
    class ExistsByCode {

        @Test
        @DisplayName("Mevcut kod için true döndürmeli")
        void givenExistingCode_whenExistsByCode_thenShouldReturnTrue() {
            // When
            boolean exists = productRepository.existsByCode("PAP_CUP_M");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Mevcut olmayan kod için false döndürmeli")
        void givenNonExistingCode_whenExistsByCode_thenShouldReturnFalse() {
            // When
            boolean exists = productRepository.existsByCode("NON_EXISTING");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Null kod için false döndürmeli")
        void givenNullCode_whenExistsByCode_thenShouldReturnFalse() {
            // When
            boolean exists = productRepository.existsByCode(null);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Büyük/küçük harf duyarlı olmalı")
        void givenDifferentCase_whenExistsByCode_thenShouldBeCaseSensitive() {
            // When
            boolean existsLowerCase = productRepository.existsByCode("pap_cup_m");
            boolean existsUpperCase = productRepository.existsByCode("PAP_CUP_M");

            // Then
            assertThat(existsLowerCase).isFalse();
            assertThat(existsUpperCase).isTrue();
        }
    }

    @Nested
    @DisplayName("Aktif ürün arama")
    class FindByActiveTrue {

        @Test
        @DisplayName("Sadece aktif ürünleri döndürmeli")
        void whenFindByActiveTrue_thenShouldReturnOnlyActiveProducts() {
            // When
            List<Product> activeProducts = productRepository.findByActiveTrue();

            // Then
            assertThat(activeProducts).hasSize(1);
            assertThat(activeProducts.get(0).getCode()).isEqualTo("PAP_CUP_M");
            assertThat(activeProducts.get(0).isActive()).isTrue();
        }

        @Test
        @DisplayName("Aktif ürün yoksa boş liste döndürmeli")
        void givenNoActiveProducts_whenFindByActiveTrue_thenShouldReturnEmptyList() {
            // Given - Tüm ürünleri pasif yap
            activeProduct.setActive(false);
            entityManager.persistAndFlush(activeProduct);

            // When
            List<Product> activeProducts = productRepository.findByActiveTrue();

            // Then
            assertThat(activeProducts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Pasif ürün arama")
    class FindByActiveFalse {

        @Test
        @DisplayName("Sadece pasif ürünleri döndürmeli")
        void whenFindByActiveFalse_thenShouldReturnOnlyInactiveProducts() {
            // When
            List<Product> inactiveProducts = productRepository.findByActiveFalse();

            // Then
            assertThat(inactiveProducts).hasSize(1);
            assertThat(inactiveProducts.get(0).getCode()).isEqualTo("PAP_CUP_L");
            assertThat(inactiveProducts.get(0).isActive()).isFalse();
        }

        @Test
        @DisplayName("Pasif ürün yoksa boş liste döndürmeli")
        void givenNoInactiveProducts_whenFindByActiveFalse_thenShouldReturnEmptyList() {
            // Given - Tüm ürünleri aktif yap
            inactiveProduct.setActive(true);
            entityManager.persistAndFlush(inactiveProduct);

            // When
            List<Product> inactiveProducts = productRepository.findByActiveFalse();

            // Then
            assertThat(inactiveProducts).isEmpty();
        }
    }

    @Nested
    @DisplayName("Ürün kaydetme ve güncelleme")
    class SaveAndUpdate {

        @Test
        @DisplayName("Yeni ürün kaydedildiğinde ID atanmalı")
        void givenNewProduct_whenSave_thenShouldAssignId() {
            // Given
            Product newProduct = Product.builder()
                    .code("NEW_PRODUCT")
                    .name("Yeni Ürün")
                    .unit("adet")
                    .unitPrice(new BigDecimal("5.00"))
                    .active(true)
                    .build();

            // When
            Product savedProduct = productRepository.save(newProduct);

            // Then
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getCode()).isEqualTo("NEW_PRODUCT");
        }

        @Test
        @DisplayName("Mevcut ürün güncellendiğinde değişiklikler kaydedilmeli")
        void givenExistingProduct_whenUpdate_thenShouldSaveChanges() {
            // Given
            activeProduct.setName("Güncellenmiş İsim");
            activeProduct.setUnitPrice(new BigDecimal("10.00"));

            // When
            Product updatedProduct = productRepository.save(activeProduct);

            // Then
            assertThat(updatedProduct.getName()).isEqualTo("Güncellenmiş İsim");
            assertThat(updatedProduct.getUnitPrice()).isEqualTo(new BigDecimal("10.00"));
        }
    }

    @Nested
    @DisplayName("Ürün silme")
    class DeleteProduct {

        @Test
        @DisplayName("Mevcut ürün silindiğinde veritabanından kaldırılmalı")
        void givenExistingProduct_whenDelete_thenShouldRemoveFromDatabase() {
            // Given
            Long productId = activeProduct.getId();

            // When
            productRepository.delete(activeProduct);

            // Then
            Optional<Product> deletedProduct = productRepository.findById(productId);
            assertThat(deletedProduct).isEmpty();
        }

        @Test
        @DisplayName("ID ile ürün silindiğinde veritabanından kaldırılmalı")
        void givenExistingProductId_whenDeleteById_thenShouldRemoveFromDatabase() {
            // Given
            Long productId = activeProduct.getId();

            // When
            productRepository.deleteById(productId);

            // Then
            Optional<Product> deletedProduct = productRepository.findById(productId);
            assertThat(deletedProduct).isEmpty();
        }
    }

    @Nested
    @DisplayName("Ürün arama ve sayma")
    class FindAndCount {

        @Test
        @DisplayName("Tüm ürünler listelendiğinde doğru sayıda döndürmeli")
        void whenFindAll_thenShouldReturnAllProducts() {
            // When
            List<Product> allProducts = productRepository.findAll();

            // Then
            assertThat(allProducts).hasSize(2);
        }

        @Test
        @DisplayName("Toplam ürün sayısı doğru olmalı")
        void whenCount_thenShouldReturnCorrectCount() {
            // When
            long count = productRepository.count();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("ID ile ürün bulunduğunda doğru ürün döndürmeli")
        void givenValidId_whenFindById_thenShouldReturnCorrectProduct() {
            // When
            Optional<Product> foundProduct = productRepository.findById(activeProduct.getId());

            // Then
            assertThat(foundProduct).isPresent();
            assertThat(foundProduct.get().getCode()).isEqualTo("PAP_CUP_M");
        }

        @Test
        @DisplayName("Geçersiz ID ile arama yapıldığında boş döndürmeli")
        void givenInvalidId_whenFindById_thenShouldReturnEmpty() {
            // When
            Optional<Product> foundProduct = productRepository.findById(999L);

            // Then
            assertThat(foundProduct).isEmpty();
        }
    }
} 