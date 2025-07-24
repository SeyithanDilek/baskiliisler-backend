package com.baskiliisler.backend.config;


import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class StartupDataConfig {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (repo.findByEmail("admin@local").isEmpty()) {
                repo.save(User.builder()
                        .name("Admin")
                        .email("admin@local")
                        .phoneNumber("+90 555 000 00 00")
                        .passwordHash(encoder.encode("admin123"))
                        .role(Role.SUPER_ADMIN)
                        .build());
                System.out.println("✅ Admin kullanıcısı oluşturuldu → admin@local / admin123");
            } else {
                System.out.println("ℹ️  Admin kullanıcısı zaten var.");
            }
        };
    }
}