package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.type.ProcessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BrandProcessRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BrandProcessRepository brandProcessRepository;

    private Brand testBrand;
    private BrandProcess testBrandProcess;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .name("Test Brand")
                .build();
        entityManager.persist(testBrand);

        testBrandProcess = BrandProcess.builder()
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .build();
    }

    @Test
    @DisplayName("Verilen marka ID'si ile süreç bulunduğunda")
    void whenFindByBrandId_thenReturnBrandProcess() {
        // given
        entityManager.persist(testBrandProcess);
        entityManager.flush();

        // when
        var found = brandProcessRepository.findByBrandId(testBrand.getId());

        // then
        assertThat(found).isPresent()
                .hasValueSatisfying(process -> {
                    assertThat(process.getBrand().getId()).isEqualTo(testBrand.getId());
                    assertThat(process.getStatus()).isEqualTo(ProcessStatus.SAMPLE_LEFT);
                });
    }

    @Test
    @DisplayName("Olmayan marka ID'si ile süreç arandığında")
    void whenFindByBrandId_withNonExistingId_thenReturnEmpty() {
        // given
        Long nonExistingId = 999L;

        // when
        var result = brandProcessRepository.findByBrandId(nonExistingId);

        // then
        assertThat(result).isEmpty();
    }
} 