package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD_HASH = "$2a$10$hashedPassword";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .name("Test User")
                .email(TEST_EMAIL)
                .passwordHash(TEST_PASSWORD_HASH)
                .role(Role.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("Mevcut Kullanıcı Getirme Testleri")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Başarılı mevcut kullanıcı getirme")
        void whenGetCurrentUser_thenReturnUser() {
            // given
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

                // when
                User result = userService.getCurrentUser();

                // then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(TEST_USER_ID);
                assertThat(result.getName()).isEqualTo("Test User");
                assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
                assertThat(result.getRole()).isEqualTo(Role.ADMIN);

                verify(userRepository).findById(TEST_USER_ID);
            }
        }

        @Test
        @DisplayName("Olmayan kullanıcı ID'si ile mevcut kullanıcı getirme")
        void whenGetCurrentUser_withNonExistingUser_thenThrowException() {
            // given
            try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
                mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(TEST_USER_ID);
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> userService.getCurrentUser())
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("Kullanıcı bulunamadı");

                verify(userRepository).findById(TEST_USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("Kullanıcı Adına Göre Yükleme Testleri")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Başarılı kullanıcı yükleme - ADMIN rolü")
        void whenLoadUserByUsername_withAdminUser_thenReturnUserDetails() {
            // given
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

            // when
            UserDetails result = userService.loadUserByUsername(TEST_EMAIL);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(TEST_EMAIL);
            assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(result.getAuthorities()).hasSize(1);
            assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.isAccountNonExpired()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.isCredentialsNonExpired()).isTrue();

            verify(userRepository).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("Başarılı kullanıcı yükleme - REP rolü")
        void whenLoadUserByUsername_withRepRole_thenReturnUserDetails() {
            // given
            User regularUser = User.builder()
                    .id(2L)
                    .name("Regular User")
                    .email("user@example.com")
                    .passwordHash(TEST_PASSWORD_HASH)
                    .role(Role.REP)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

            // when
            UserDetails result = userService.loadUserByUsername("user@example.com");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user@example.com");
            assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(result.getAuthorities()).hasSize(1);
            assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_REP");

            verify(userRepository).findByEmail("user@example.com");
        }

        @Test
        @DisplayName("Olmayan email ile kullanıcı yükleme")
        void whenLoadUserByUsername_withNonExistingEmail_thenThrowException() {
            // given
            String nonExistingEmail = "nonexisting@example.com";
            when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.loadUserByUsername(nonExistingEmail))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Kullanıcı bulunamadı: " + nonExistingEmail);

            verify(userRepository).findByEmail(nonExistingEmail);
        }

        @Test
        @DisplayName("Null email ile kullanıcı yükleme")
        void whenLoadUserByUsername_withNullEmail_thenCallRepository() {
            // given
            when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.loadUserByUsername(null))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Kullanıcı bulunamadı: null");

            verify(userRepository).findByEmail(null);
        }

        @Test
        @DisplayName("Boş email ile kullanıcı yükleme")
        void whenLoadUserByUsername_withEmptyEmail_thenThrowException() {
            // given
            String emptyEmail = "";
            when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.loadUserByUsername(emptyEmail))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("Kullanıcı bulunamadı: ");

            verify(userRepository).findByEmail(emptyEmail);
        }
    }
} 