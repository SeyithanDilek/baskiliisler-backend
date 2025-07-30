package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Dealer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BrandRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BrandRepository brandRepository;

    private Brand testBrand;
    private Dealer testDealer;

    @BeforeEach
    void setUp() {
        testDealer = Dealer.builder()
                .name("Test Dealer")
                .active(true)
                .build();
        entityManager.persist(testDealer);
        entityManager.flush();

        testBrand = Brand.builder()
                .name("Test Brand")
                .dealer(testDealer)
                .build();
    }

    @Test
    @DisplayName("Verilen isim ile marka bulunduğunda")
    void whenFindByName_thenReturnBrand() {
        // given
        entityManager.persist(testBrand);
        entityManager.flush();

        // when
        var found = brandRepository.findByName(testBrand.getName());

        // then
        assertThat(found).isPresent()
                .hasValueSatisfying(brand ->
                        assertThat(brand.getName()).isEqualTo(testBrand.getName()));
    }

    @Test
    @DisplayName("Olmayan isim ile marka arandığında")
    void whenFindByName_withNonExistingName_thenReturnEmpty() {
        // given
        String nonExistingName = "Non Existing Brand";

        // when
        var result = brandRepository.findByName(nonExistingName);

        // then
        assertThat(result).isEmpty();
    }
} 