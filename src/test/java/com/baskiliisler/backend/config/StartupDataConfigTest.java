package com.baskiliisler.backend.config;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartupDataConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private StartupDataConfig startupDataConfig;

    @BeforeEach
    void setUp() {
        startupDataConfig = new StartupDataConfig(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Admin kullanıcısı yokken")
    void whenNoAdminExists_thenCreateAdmin() throws Exception {
        // given
        when(userRepository.findByEmail("admin@local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // when
        startupDataConfig.seedAdmin().run();

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Admin");
        assertThat(savedUser.getEmail()).isEqualTo("admin@local");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Admin kullanıcısı zaten varken")
    void whenAdminExists_thenDoNothing() throws Exception {
        // given
        User existingAdmin = User.builder()
                .name("Existing Admin")
                .email("admin@local")
                .passwordHash("existingHash")
                .role(Role.ADMIN)
                .build();
        when(userRepository.findByEmail("admin@local")).thenReturn(Optional.of(existingAdmin));

        // when
        startupDataConfig.seedAdmin().run();

        // then
        verify(userRepository, never()).save(any());
    }
} 