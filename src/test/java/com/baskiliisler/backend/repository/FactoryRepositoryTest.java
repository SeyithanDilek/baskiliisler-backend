package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FactoryRepository Test")
class FactoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FactoryRepository factoryRepository;

    private Factory testFactory;

    @BeforeEach
    void setUp() {
        testFactory = Factory.builder()
                .name("Test Factory")
                .address("Test Address, Istanbul")
                .phoneNumber("+90 555 123 45 67")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Fabrika ID'ye göre bulma")
    void whenFindById_thenReturnFactory() {
        // given
        Factory saved = entityManager.persistAndFlush(testFactory);

        // when
        Optional<Factory> result = factoryRepository.findById(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Factory");
        assertThat(result.get().getAddress()).isEqualTo("Test Address, Istanbul");
        assertThat(result.get().getPhoneNumber()).isEqualTo("+90 555 123 45 67");
        assertThat(result.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Olmayan fabrika ID'ye göre arama")
    void whenFindById_withNonExistingId_thenReturnEmpty() {
        // when
        Optional<Factory> result = factoryRepository.findById(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Tüm fabrikaları listeleme")
    void whenFindAll_thenReturnAllFactories() {
        // given
        Factory factory1 = Factory.builder()
                .name("Factory 1")
                .address("Address 1")

                .active(true)
                .build();

        Factory factory2 = Factory.builder()
                .name("Factory 2")
                .address("Address 2")

                .active(false)
                .build();

        entityManager.persist(factory1);
        entityManager.persist(factory2);
        entityManager.flush();

        // when
        List<Factory> result = factoryRepository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Factory::getName)
                .containsExactlyInAnyOrder("Factory 1", "Factory 2");
    }

    @Test
    @DisplayName("Fabrika kaydetme")
    void whenSave_thenReturnSavedFactory() {
        // when
        Factory result = factoryRepository.save(testFactory);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Factory");
        assertThat(result.getAddress()).isEqualTo("Test Address, Istanbul");
        assertThat(result.getPhoneNumber()).isEqualTo("+90 555 123 45 67");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("Fabrika silme")
    void whenDelete_thenFactoryDeleted() {
        // given
        Factory saved = entityManager.persistAndFlush(testFactory);

        // when
        factoryRepository.deleteById(saved.getId());
        entityManager.flush();

        // then
        Optional<Factory> result = factoryRepository.findById(saved.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Aktif fabrikaları bulma")
    void whenFindByActiveTrue_thenReturnOnlyActiveFactories() {
        // given
        Factory activeFactory1 = Factory.builder()
                .name("Active Factory 1")
                .address("Active Address 1")

                .active(true)
                .build();

        Factory activeFactory2 = Factory.builder()
                .name("Active Factory 2")
                .address("Active Address 2")

                .active(true)
                .build();

        Factory inactiveFactory = Factory.builder()
                .name("Inactive Factory")
                .address("Inactive Address")

                .active(false)
                .build();

        entityManager.persist(activeFactory1);
        entityManager.persist(activeFactory2);
        entityManager.persist(inactiveFactory);
        entityManager.flush();

        // when
        List<Factory> result = factoryRepository.findByActiveTrue();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Factory::getName)
                .containsExactlyInAnyOrder("Active Factory 1", "Active Factory 2");
        assertThat(result).extracting(Factory::isActive)
                .containsOnly(true);
    }
} 