package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.ProcessHistory;
import com.baskiliisler.backend.type.ProcessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProcessHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProcessHistoryRepository processHistoryRepository;

    private Brand testBrand;
    private BrandProcess testBrandProcess;

    @BeforeEach
    void setUp() {
        testBrand = Brand.builder()
                .name("Test Brand")
                .contactEmail("test@brand.com")
                .contactPhone("1234567890")
                .build();
        
        entityManager.persist(testBrand);

        testBrandProcess = BrandProcess.builder()
                .brand(testBrand)
                .status(ProcessStatus.SAMPLE_LEFT)
                .updatedAt(LocalDateTime.now())
                .build();
        
        entityManager.persistAndFlush(testBrandProcess);
    }

    @Test
    @DisplayName("Marka ID'ye göre süreç geçmişini bulma")
    void whenFindByBrandId_thenReturnProcessHistoryList() {
        // given
        ProcessHistory history1 = ProcessHistory.builder()
                .process(testBrandProcess)
                .fromStatus(null)
                .toStatus(ProcessStatus.INIT)
                .actorId(100L)
                .changedAt(LocalDateTime.now().minusDays(2))
                .payload("{\"brandId\":" + testBrand.getId() + "}")
                .build();

        ProcessHistory history2 = ProcessHistory.builder()
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.INIT)
                .toStatus(ProcessStatus.SAMPLE_LEFT)
                .actorId(100L)
                .changedAt(LocalDateTime.now().minusDays(1))
                .payload("{\"brandId\":" + testBrand.getId() + "}")
                .build();

        ProcessHistory history3 = ProcessHistory.builder()
                .process(testBrandProcess)
                .fromStatus(ProcessStatus.SAMPLE_LEFT)
                .toStatus(ProcessStatus.OFFER_SENT)
                .actorId(200L)
                .changedAt(LocalDateTime.now())
                .payload("{\"quoteId\":123}")
                .build();

        entityManager.persist(history1);
        entityManager.persist(history2);
        entityManager.persist(history3);
        entityManager.flush();

        // when
        List<ProcessHistory> result = processHistoryRepository.findByBrandId(testBrand.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(ProcessHistory::getToStatus)
                .containsExactlyInAnyOrder(
                        ProcessStatus.INIT,
                        ProcessStatus.SAMPLE_LEFT,
                        ProcessStatus.OFFER_SENT
                );
        assertThat(result).extracting(ProcessHistory::getActorId)
                .containsExactlyInAnyOrder(100L, 100L, 200L);
    }

    @Test
    @DisplayName("Olmayan marka ID'ye göre süreç geçmişi arama")
    void whenFindByBrandId_withNonExistingBrandId_thenReturnEmptyList() {
        // when
        List<ProcessHistory> result = processHistoryRepository.findByBrandId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Süreç geçmişi olmayan marka için arama")
    void whenFindByBrandId_withBrandWithoutHistory_thenReturnEmptyList() {
        // given
        Brand brandWithoutHistory = Brand.builder()
                .name("Brand Without History")
                .contactEmail("nohistory@brand.com")
                .contactPhone("1111111111")
                .build();
        
        Brand saved = entityManager.persistAndFlush(brandWithoutHistory);

        // when
        List<ProcessHistory> result = processHistoryRepository.findByBrandId(saved.getId());

        // then
        assertThat(result).isEmpty();
    }
} 