package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Dealer;
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
class DealerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DealerRepository dealerRepository;

    private Dealer testDealer;

    @BeforeEach
    void setUp() {
        testDealer = Dealer.builder()
                .name("Test Dealer")
                .address("Test Address")
                .phoneNumber("+90 555 123 45 67")
                .taxNumber("1234567890")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Bayi kaydedildiğinde")
    void whenSaveDealer_thenReturnSavedDealer() {
        // given
        // when
        Dealer savedDealer = dealerRepository.save(testDealer);

        // then
        assertThat(savedDealer.getId()).isNotNull();
        assertThat(savedDealer.getName()).isEqualTo(testDealer.getName());
        assertThat(savedDealer.getAddress()).isEqualTo(testDealer.getAddress());
        assertThat(savedDealer.getPhoneNumber()).isEqualTo(testDealer.getPhoneNumber());
        assertThat(savedDealer.getTaxNumber()).isEqualTo(testDealer.getTaxNumber());
        assertThat(savedDealer.isActive()).isTrue();
    }

    @Test
    @DisplayName("Aktif bayiler listelendiğinde")
    void whenFindByActive_thenReturnActiveDealers() {
        // given
        Dealer activeDealer = Dealer.builder()
                .name("Active Dealer")
                .active(true)
                .build();
        
        Dealer inactiveDealer = Dealer.builder()
                .name("Inactive Dealer")
                .active(false)
                .build();
        
        entityManager.persist(activeDealer);
        entityManager.persist(inactiveDealer);
        entityManager.flush();

        // when
        List<Dealer> activeDealers = dealerRepository.findByActive(true);

        // then
        assertThat(activeDealers).hasSize(1);
        assertThat(activeDealers.get(0).getName()).isEqualTo("Active Dealer");
    }

    @Test
    @DisplayName("Bayi adı ile arandığında")
    void whenFindByName_thenReturnDealer() {
        // given
        entityManager.persist(testDealer);
        entityManager.flush();

        // when
        Optional<Dealer> found = dealerRepository.findByName(testDealer.getName());

        // then
        assertThat(found).isPresent()
                .hasValueSatisfying(dealer -> {
                    assertThat(dealer.getName()).isEqualTo(testDealer.getName());
                    assertThat(dealer.getAddress()).isEqualTo(testDealer.getAddress());
                });
    }

    @Test
    @DisplayName("Bayi adı kontrolü yapıldığında")
    void whenExistsByName_thenReturnTrue() {
        // given
        entityManager.persist(testDealer);
        entityManager.flush();

        // when
        boolean exists = dealerRepository.existsByName(testDealer.getName());

        // then
        assertThat(exists).isTrue();
    }
} 