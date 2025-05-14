package com.baskiliisler.backend.config;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private User testUser;
    private static final String TEST_TOKEN = "Bearer test.jwt.token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .role(Role.REP)
                .build();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Geçerli JWT token ile istek yapıldığında")
    void whenValidJwtToken_thenSetAuthentication() throws Exception {
        // given
        Claims mockClaims = mock(Claims.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_TOKEN);
        when(jwtUtil.parse(anyString())).thenReturn(mockClaims);
        when(mockClaims.getSubject()).thenReturn(testUser.getId().toString());
        when(mockClaims.get("role", String.class)).thenReturn(testUser.getRole().name());
        when(userRepository.existsById(testUser.getId())).thenReturn(true);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Authorization header olmadığında")
    void whenNoAuthorizationHeader_thenContinueChain() throws Exception {
        // given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Geçersiz JWT token formatı ile istek yapıldığında")
    void whenInvalidTokenFormat_thenContinueChain() throws Exception {
        // given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidTokenFormat");

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("JWT token parse edilemediğinde")
    void whenTokenParsingFails_thenThrowException() throws Exception {
        // given
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TEST_TOKEN);
        when(jwtUtil.parse(anyString())).thenThrow(new RuntimeException("Token parsing failed"));

        // when & then
        try {
            jwtFilter.doFilterInternal(request, response, filterChain);
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("Token parsing failed");
        }
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
} 