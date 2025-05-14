package com.baskiliisler.backend.config;

import com.baskiliisler.backend.repository.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return Mockito.mock(PasswordEncoder.class);
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return new JwtUtil("test-secret-key-test-secret-key-test-secret-key-test", "30m");
    }

    @Bean
    @Primary
    public JwtFilter jwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        return new JwtFilter(jwtUtil, userRepository);
    }
}
