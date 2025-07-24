package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.dto.UserResponseDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

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
                .phoneNumber("+90 555 123 45 67")
                .passwordHash(TEST_PASSWORD_HASH)
                .role(Role.SUPER_ADMIN)
                .build();
    }

    @Nested
    @DisplayName("Kullanıcı Oluşturma Testleri")
    class CreateUserTests {

        @Test
        @DisplayName("Başarılı kullanıcı oluşturma")
        void whenCreateUser_thenReturnUserResponseDto() {
            // given
            UserCreateDto createDto = UserCreateDto.builder()
                    .name("New User")
                    .email("newuser@example.com")
                    .phoneNumber("+90 555 999 99 99")
                    .build();

            String plainPassword = "TestPass123!";
            String hashedPassword = "$2a$10$hashedPassword";

            when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn(hashedPassword);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // when
            UserResponseDto result = userService.createUser(createDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("New User");
            assertThat(result.email()).isEqualTo("newuser@example.com");
            assertThat(result.phoneNumber()).isEqualTo("+90 555 999 99 99");
            assertThat(result.role()).isEqualTo(Role.DEALER_USER);

            verify(userRepository).existsByEmail(createDto.getEmail());
            verify(passwordEncoder).encode(any());
            verify(userRepository).save(any(User.class));
            verify(emailService).sendWelcomeEmail(eq(createDto.getEmail()), eq(createDto.getName()), any());
        }

        @Test
        @DisplayName("Mevcut email ile kullanıcı oluşturma")
        void whenCreateUser_withExistingEmail_thenThrowException() {
            // given
            UserCreateDto createDto = UserCreateDto.builder()
                    .name("New User")
                    .email("existing@example.com")
                    .phoneNumber("+90 555 999 99 99")
                    .build();

            when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.createUser(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bu email adresi zaten kullanılıyor");

            verify(userRepository).existsByEmail(createDto.getEmail());
            verify(userRepository, never()).save(any());
            verify(emailService, never()).sendWelcomeEmail(any(), any(), any());
        }
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
                assertThat(result.getRole()).isEqualTo(Role.SUPER_ADMIN);

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
        @DisplayName("Başarılı kullanıcı yükleme - SUPER_ADMIN rolü")
        void whenLoadUserByUsername_withSuperAdminUser_thenReturnUserDetails() {
            // given
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

            // when
            UserDetails result = userService.loadUserByUsername(TEST_EMAIL);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(TEST_EMAIL);
            assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(result.getAuthorities()).hasSize(1);
            assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_SUPER_ADMIN");
            assertThat(result.isEnabled()).isTrue();
            assertThat(result.isAccountNonExpired()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
            assertThat(result.isCredentialsNonExpired()).isTrue();

            verify(userRepository).findByEmail(TEST_EMAIL);
        }

        @Test
        @DisplayName("Başarılı kullanıcı yükleme - DEALER_USER rolü")
        void whenLoadUserByUsername_withDealerUserRole_thenReturnUserDetails() {
            // given
            User regularUser = User.builder()
                    .id(2L)
                    .name("Regular User")
                    .email("user@example.com")
                    .phoneNumber("+90 555 123 45 67")
                    .passwordHash(TEST_PASSWORD_HASH)
                    .role(Role.DEALER_USER)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

            // when
            UserDetails result = userService.loadUserByUsername("user@example.com");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user@example.com");
            assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(result.getAuthorities()).hasSize(1);
            assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_DEALER_USER");

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