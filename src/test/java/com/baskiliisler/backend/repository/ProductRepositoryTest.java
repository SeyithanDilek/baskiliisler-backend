package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.type.Unit;
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
    private Dealer testDealer;

    @BeforeEach
    void setUp() {
        testDealer = Dealer.builder()
                .name("Test Dealer")
                .active(true)
                .build();
        entityManager.persist(testDealer);
        entityManager.flush();

        activeProduct = Product.builder()
                .name("Orta Karton Bardak")
                .description("Orta boy karton bardak açıklaması")
                .unit(Unit.ADET)
                .unitPrice(new BigDecimal("2.50"))
                .taxRate(new BigDecimal("18.00"))
                .active(true)
                .dealer(testDealer)
                .build();

        inactiveProduct = Product.builder()
                .name("Büyük Karton Bardak")
                .description("Büyük boy karton bardak açıklaması")
                .unit(Unit.ADET)
                .unitPrice(new BigDecimal("3.50"))
                .taxRate(new BigDecimal("18.00"))
                .active(false)
                .dealer(testDealer)
                .build();

        entityManager.persistAndFlush(activeProduct);
        entityManager.persistAndFlush(inactiveProduct);
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
            assertThat(activeProducts.get(0).getName()).isEqualTo("Orta Karton Bardak");
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
            assertThat(inactiveProducts.get(0).getName()).isEqualTo("Büyük Karton Bardak");
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
                    .name("Yeni Ürün")
                    .description("Yeni ürün açıklaması")
                    .unit(Unit.KG)
                    .unitPrice(new BigDecimal("5.00"))
                    .taxRate(new BigDecimal("8.00"))
                    .active(true)
                    .build();

            // When
            Product savedProduct = productRepository.save(newProduct);

            // Then
            assertThat(savedProduct.getId()).isNotNull();
            assertThat(savedProduct.getName()).isEqualTo("Yeni Ürün");
            assertThat(savedProduct.getDescription()).isEqualTo("Yeni ürün açıklaması");
            assertThat(savedProduct.getUnit()).isEqualTo(Unit.KG);
            assertThat(savedProduct.getTaxRate()).isEqualTo(new BigDecimal("8.00"));
        }

        @Test
        @DisplayName("Mevcut ürün güncellendiğinde değişiklikler kaydedilmeli")
        void givenExistingProduct_whenUpdate_thenShouldSaveChanges() {
            // Given
            activeProduct.setName("Güncellenmiş İsim");
            activeProduct.setDescription("Güncellenmiş açıklama");
            activeProduct.setUnitPrice(new BigDecimal("10.00"));
            activeProduct.setTaxRate(new BigDecimal("20.00"));

            // When
            Product updatedProduct = productRepository.save(activeProduct);

            // Then
            assertThat(updatedProduct.getName()).isEqualTo("Güncellenmiş İsim");
            assertThat(updatedProduct.getDescription()).isEqualTo("Güncellenmiş açıklama");
            assertThat(updatedProduct.getUnitPrice()).isEqualTo(new BigDecimal("10.00"));
            assertThat(updatedProduct.getTaxRate()).isEqualTo(new BigDecimal("20.00"));
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
            assertThat(foundProduct.get().getName()).isEqualTo("Orta Karton Bardak");
            assertThat(foundProduct.get().getDescription()).isEqualTo("Orta boy karton bardak açıklaması");
            assertThat(foundProduct.get().getUnit()).isEqualTo(Unit.ADET);
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