package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.config.JwtFilter;
import com.baskiliisler.backend.config.JwtUtil;
import com.baskiliisler.backend.config.SecurityConfig;
import com.baskiliisler.backend.config.TestSecurityConfig;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();
    }

    @Test
    @DisplayName("Geçerli kimlik bilgileriyle giriş yapıldığında")
    void whenLogin_withValidCredentials_thenReturnToken() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";
        String token = "test.jwt.token";
        
        when(authService.login(email, password)).thenReturn(token);

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "test@example.com",
                            "password": "password123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    @DisplayName("Geçersiz kimlik bilgileriyle giriş yapıldığında")
    void whenLogin_withInvalidCredentials_thenReturnUnauthorized() throws Exception {
        // given
        when(authService.login(anyString(), anyString()))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        // when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email": "wrong@example.com",
                            "password": "wrongpass"
                        }
                        """))
                .andExpect(status().isUnauthorized());
    }
} 