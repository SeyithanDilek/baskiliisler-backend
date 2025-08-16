package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ForgotPasswordRequestDto;
import com.baskiliisler.backend.dto.PasswordResetResponseDto;
import com.baskiliisler.backend.dto.ResetPasswordRequestDto;
import com.baskiliisler.backend.model.PasswordResetToken;
import com.baskiliisler.backend.repository.PasswordResetTokenRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

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
                .phoneNumber("+90 555 123 45 67")
                .passwordHash("hashedPassword")
                .role(Role.SUPER_ADMIN)
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

    @Test
    @DisplayName("Şifremi unuttum isteği geçerli email ile yapıldığında")
    void whenForgotPassword_withValidEmail_thenReturnSuccessResponse() {
        // given
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(new PasswordResetToken());

        // when
        PasswordResetResponseDto response = authService.forgotPassword(request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).contains("Şifre sıfırlama linki email adresinize gönderildi");
    }

    @Test
    @DisplayName("Şifremi unuttum isteği olmayan email ile yapıldığında")
    void whenForgotPassword_withNonExistingEmail_thenThrowException() {
        // given
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Bu email adresi ile kayıtlı kullanıcı bulunamadı");
    }

    @Test
    @DisplayName("Şifre sıfırlama geçerli token ile yapıldığında")
    void whenResetPassword_withValidToken_thenReturnSuccessResponse() {
        // given
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("valid-token", "newPassword123");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(testUser)
                .token("valid-token")
                .expiresAt(java.time.LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        
        when(passwordResetTokenRepository.findValidToken(eq("valid-token"), any()))
                .thenReturn(Optional.of(resetToken));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        PasswordResetResponseDto response = authService.resetPassword(request);

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).contains("Şifre başarıyla güncellendi");
    }

    @Test
    @DisplayName("Şifre sıfırlama geçersiz token ile yapılmaya çalışıldığında")
    void whenResetPassword_withInvalidToken_thenThrowException() {
        // given
        ResetPasswordRequestDto request = new ResetPasswordRequestDto("invalid-token", "newPassword123");
        when(passwordResetTokenRepository.findValidToken(eq("invalid-token"), any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Geçersiz veya süresi dolmuş token");
    }
} 