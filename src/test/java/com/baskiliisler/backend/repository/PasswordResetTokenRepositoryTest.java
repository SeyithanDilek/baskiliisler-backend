package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.PasswordResetToken;
import com.baskiliisler.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+90 555 123 45 67")
                .passwordHash("hashedPassword")
                .role(Role.DEALER_USER)
                .build();
        testUser = userRepository.save(testUser);

        testToken = PasswordResetToken.builder()
                .user(testUser)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        testToken = passwordResetTokenRepository.save(testToken);
    }

    @Test
    void findByToken_ShouldReturnToken_WhenTokenExists() {
        Optional<PasswordResetToken> found = passwordResetTokenRepository.findByToken(testToken.getToken());
        
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(testToken.getToken());
    }

    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        Optional<PasswordResetToken> found = passwordResetTokenRepository.findByToken("non-existent-token");
        
        assertThat(found).isEmpty();
    }

    @Test
    void findByTokenAndUsedFalse_ShouldReturnToken_WhenTokenExistsAndNotUsed() {
        Optional<PasswordResetToken> found = passwordResetTokenRepository.findByTokenAndUsedFalse(testToken.getToken());
        
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(testToken.getToken());
    }

    @Test
    void findByTokenAndUsedFalse_ShouldReturnEmpty_WhenTokenIsUsed() {
        testToken.setUsed(true);
        passwordResetTokenRepository.save(testToken);

        Optional<PasswordResetToken> found = passwordResetTokenRepository.findByTokenAndUsedFalse(testToken.getToken());
        
        assertThat(found).isEmpty();
    }

    @Test
    void findValidToken_ShouldReturnToken_WhenTokenIsValid() {
        Optional<PasswordResetToken> found = passwordResetTokenRepository.findValidToken(
                testToken.getToken(), LocalDateTime.now());
        
        assertThat(found).isPresent();
        assertThat(found.get().getToken()).isEqualTo(testToken.getToken());
    }

    @Test
    void findValidToken_ShouldReturnEmpty_WhenTokenIsExpired() {
        testToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        passwordResetTokenRepository.save(testToken);

        Optional<PasswordResetToken> found = passwordResetTokenRepository.findValidToken(
                testToken.getToken(), LocalDateTime.now());
        
        assertThat(found).isEmpty();
    }

    @Test
    void findValidToken_ShouldReturnEmpty_WhenTokenIsUsed() {
        testToken.setUsed(true);
        passwordResetTokenRepository.save(testToken);

        Optional<PasswordResetToken> found = passwordResetTokenRepository.findValidToken(
                testToken.getToken(), LocalDateTime.now());
        
        assertThat(found).isEmpty();
    }

    @Test
    void invalidateUserTokens_ShouldMarkAllUserTokensAsUsed() {
        // İkinci bir token oluştur
        PasswordResetToken secondToken = PasswordResetToken.builder()
                .user(testUser)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(secondToken);

        passwordResetTokenRepository.invalidateUserTokens(testUser.getId());

        // EntityManager'ı flush et
        entityManager.flush();
        entityManager.clear();

        // Tüm token'ların used olarak işaretlendiğini kontrol et
        Optional<PasswordResetToken> firstToken = passwordResetTokenRepository.findById(testToken.getId());
        Optional<PasswordResetToken> secondTokenFound = passwordResetTokenRepository.findById(secondToken.getId());

        assertThat(firstToken).isPresent();
        assertThat(firstToken.get().getUsed()).isTrue();
        assertThat(secondTokenFound).isPresent();
        assertThat(secondTokenFound.get().getUsed()).isTrue();
    }
}
