package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Ürün ID'ye göre bulma")
    void whenFindById_thenReturnProduct() {
        // given
        Product product = Product.builder()
                .code("TEST_PROD")
                .name("Test Product")
                .unit("adet")
                .unitPrice(BigDecimal.valueOf(100))
                .active(true)
                .build();
        
        Product saved = entityManager.persistAndFlush(product);

        // when
        Optional<Product> result = productRepository.findById(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("TEST_PROD");
        assertThat(result.get().getName()).isEqualTo("Test Product");
        assertThat(result.get().getUnit()).isEqualTo("adet");
        assertThat(result.get().getUnitPrice()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Olmayan ürün ID'ye göre arama")
    void whenFindById_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<Product> result = productRepository.findById(999L);

        // then
        assertThat(result).isEmpty();
    }
} 