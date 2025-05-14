package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(Role.REP)
                .build();
    }

    @Test
    @DisplayName("Verilen e-posta ile kullanıcı bulunduğunda")
    void whenFindByEmail_thenReturnUser() {
        // given
        entityManager.persist(testUser);
        entityManager.flush();

        // when
        var found = userRepository.findByEmail(testUser.getEmail());

        // then
        assertThat(found).isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getName()).isEqualTo(testUser.getName());
                    assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
                    assertThat(user.getPasswordHash()).isEqualTo(testUser.getPasswordHash());
                    assertThat(user.getRole()).isEqualTo(testUser.getRole());
                });
    }

    @Test
    @DisplayName("Olmayan e-posta ile kullanıcı arandığında")
    void whenFindByEmail_withNonExistingEmail_thenReturnEmpty() {
        // given
        String nonExistingEmail = "nonexisting@example.com";

        // when
        var result = userRepository.findByEmail(nonExistingEmail);

        // then
        assertThat(result).isEmpty();
    }
} 