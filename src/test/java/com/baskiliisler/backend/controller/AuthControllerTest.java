package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.GlobalExceptionHandler;
import com.baskiliisler.backend.dto.*;
import com.baskiliisler.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private LoginRequestDto testLoginRequest;
    private RegisterRequestDto testRegisterRequest;
    private ChangePasswordRequestDto testChangePasswordRequest;
    private AuthResponseDto testAuthResponse;
    private UserResponseDto testUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testLoginRequest = new LoginRequestDto(
                "test@example.com",
                "password123"
        );

        testRegisterRequest = new RegisterRequestDto(
                "Test User",
                "test@example.com",
                "password123",
                Role.REP
        );

        testChangePasswordRequest = new ChangePasswordRequestDto(
                "oldPassword123",
                "newPassword123"
        );

        testUserResponse = new UserResponseDto(
                1L,
                "Test User",
                "test@example.com",
                Role.REP
        );

        testAuthResponse = new AuthResponseDto(
                "jwt-token-here",
                testUserResponse
        );
    }

    @Nested
    @DisplayName("POST /auth/login - Giriş yapma")
    class Login {

        @Test
        @DisplayName("Geçerli bilgilerle giriş yapıldığında 200 OK döndürmeli")
        void givenValidCredentials_whenLogin_thenShouldReturn200() throws Exception {
            // Given
            when(authService.login(any(LoginRequestDto.class))).thenReturn(testAuthResponse);

            // When & Then
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(testAuthResponse.token()))
                    .andExpect(jsonPath("$.type").value(testAuthResponse.type()))
                    .andExpect(jsonPath("$.user.id").value(testUserResponse.id()))
                    .andExpect(jsonPath("$.user.email").value(testUserResponse.email()));

            verify(authService).login(any(LoginRequestDto.class));
        }

        @Test
        @DisplayName("Geçersiz bilgilerle giriş yapıldığında 401 Unauthorized döndürmeli")
        void givenInvalidCredentials_whenLogin_thenShouldReturn401() throws Exception {
            // Given
            when(authService.login(any(LoginRequestDto.class)))
                    .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Kullanıcı bulunamadı"));

            // When & Then
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testLoginRequest)))
                    .andExpect(status().isUnauthorized());

            verify(authService).login(any(LoginRequestDto.class));
        }

        @Test
        @DisplayName("Geçersiz verilerle giriş yapıldığında 400 Bad Request döndürmeli")
        void givenInvalidData_whenLogin_thenShouldReturn400() throws Exception {
            // Given
            LoginRequestDto invalidRequest = new LoginRequestDto("", "");

            // When & Then
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any(LoginRequestDto.class));
        }
    }

    @Nested
    @DisplayName("POST /auth/register - Kayıt olma")
    class Register {

        @Test
        @DisplayName("Geçerli verilerle kayıt olunduğunda 201 Created döndürmeli")
        void givenValidData_whenRegister_thenShouldReturn201() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDto.class))).thenReturn(testAuthResponse);

            // When & Then
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRegisterRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value(testAuthResponse.token()))
                    .andExpect(jsonPath("$.user.name").value(testUserResponse.name()))
                    .andExpect(jsonPath("$.user.email").value(testUserResponse.email()));

            verify(authService).register(any(RegisterRequestDto.class));
        }

        @Test
        @DisplayName("Mevcut email ile kayıt olunmaya çalışıldığında 400 Bad Request döndürmeli")
        void givenExistingEmail_whenRegister_thenShouldReturn400() throws Exception {
            // Given
            when(authService.register(any(RegisterRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Bu email adresi zaten kullanılıyor"));

            // When & Then
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Bu email adresi zaten kullanılıyor"));

            verify(authService).register(any(RegisterRequestDto.class));
        }

        @Test
        @DisplayName("Geçersiz verilerle kayıt olunmaya çalışıldığında 400 Bad Request döndürmeli")
        void givenInvalidData_whenRegister_thenShouldReturn400() throws Exception {
            // Given
            RegisterRequestDto invalidRequest = new RegisterRequestDto("", "invalid-email", "123", Role.REP);

            // When & Then
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any(RegisterRequestDto.class));
        }
    }

    @Nested
    @DisplayName("POST /auth/change-password - Şifre değiştirme")
    class ChangePassword {

        @Test
        @DisplayName("Geçerli verilerle şifre değiştirildiğinde 204 No Content döndürmeli")
        void givenValidData_whenChangePassword_thenShouldReturn204() throws Exception {
            // Given
            doNothing().when(authService).changePassword(any(ChangePasswordRequestDto.class));

            // When & Then
            mockMvc.perform(post("/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testChangePasswordRequest)))
                    .andExpect(status().isNoContent());

            verify(authService).changePassword(any(ChangePasswordRequestDto.class));
        }

        @Test
        @DisplayName("Yanlış mevcut şifre ile değiştirme yapıldığında 400 Bad Request döndürmeli")
        void givenWrongCurrentPassword_whenChangePassword_thenShouldReturn400() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Mevcut şifre hatalı"))
                    .when(authService).changePassword(any(ChangePasswordRequestDto.class));

            // When & Then
            mockMvc.perform(post("/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testChangePasswordRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Mevcut şifre hatalı"));

            verify(authService).changePassword(any(ChangePasswordRequestDto.class));
        }

        @Test
        @DisplayName("Geçersiz verilerle şifre değiştirme yapıldığında 400 Bad Request döndürmeli")
        void givenInvalidData_whenChangePassword_thenShouldReturn400() throws Exception {
            // Given
            ChangePasswordRequestDto invalidRequest = new ChangePasswordRequestDto("", "123");

            // When & Then
            mockMvc.perform(post("/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).changePassword(any(ChangePasswordRequestDto.class));
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh - Token yenileme")
    class RefreshToken {

        @Test
        @DisplayName("Geçerli token ile yenilendiğinde 200 OK döndürmeli")
        void givenValidToken_whenRefreshToken_thenShouldReturn200() throws Exception {
            // Given
            String token = "Bearer jwt-token-here";
            when(authService.refreshToken(token)).thenReturn(testAuthResponse);

            // When & Then
            mockMvc.perform(post("/auth/refresh")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value(testAuthResponse.token()))
                    .andExpect(jsonPath("$.user.id").value(testUserResponse.id()));

            verify(authService).refreshToken(token);
        }

        @Test
        @DisplayName("Geçersiz token ile yenilenmeye çalışıldığında 404 Not Found döndürmeli")
        void givenInvalidToken_whenRefreshToken_thenShouldReturn404() throws Exception {
            // Given
            String token = "Bearer invalid-token";
            when(authService.refreshToken(token))
                    .thenThrow(new EntityNotFoundException("Kullanıcı bulunamadı"));

            // When & Then
            mockMvc.perform(post("/auth/refresh")
                            .header("Authorization", token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));

            verify(authService).refreshToken(token);
        }
    }

    @Nested
    @DisplayName("POST /auth/logout - Çıkış yapma")
    class Logout {

        @Test
        @DisplayName("Çıkış yapıldığında 204 No Content döndürmeli")
        void whenLogout_thenShouldReturn204() throws Exception {
            // Given
            doNothing().when(authService).logout();

            // When & Then
            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isNoContent());

            verify(authService).logout();
        }
    }
} 