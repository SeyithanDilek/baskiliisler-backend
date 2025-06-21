package com.baskiliisler.backend.service;

import com.baskiliisler.backend.security.JwtService;
import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email(TEST_EMAIL)
                .passwordHash("hashedPassword")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Geçerli kimlik bilgileriyle giriş yapıldığında")
    void whenLogin_withValidCredentials_thenReturnToken() {
        // given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn(TEST_TOKEN);

        // when
        String token = authService.login(TEST_EMAIL, TEST_PASSWORD);

        // then
        assertThat(token).isEqualTo(TEST_TOKEN);
    }

    @Test
    @DisplayName("Olmayan kullanıcı ile giriş yapılmaya çalışıldığında")
    void whenLogin_withNonExistingUser_thenThrowException() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(TEST_EMAIL, TEST_PASSWORD))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Kullanıcı bulunamadı");
    }

    @Test
    @DisplayName("Yanlış şifre ile giriş yapılmaya çalışıldığında")
    void whenLogin_withInvalidPassword_thenThrowException() {
        // given
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPasswordHash())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(TEST_EMAIL, TEST_PASSWORD))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Şifre hatalı");
    }
} 