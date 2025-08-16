package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.AuthResponseDto;
import com.baskiliisler.backend.dto.ChangePasswordRequestDto;
import com.baskiliisler.backend.dto.ForgotPasswordRequestDto;
import com.baskiliisler.backend.dto.LoginRequestDto;
import com.baskiliisler.backend.dto.PasswordResetResponseDto;
import com.baskiliisler.backend.dto.RegisterRequestDto;
import com.baskiliisler.backend.dto.ResetPasswordRequestDto;
import com.baskiliisler.backend.mapper.UserMapper;
import com.baskiliisler.backend.model.PasswordResetToken;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.PasswordResetTokenRepository;
import com.baskiliisler.backend.security.JwtService;
import com.baskiliisler.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthResponseDto login(LoginRequestDto request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı bulunamadı"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Şifre hatalı");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token, UserMapper.toResponseDto(user));
    }

    // Backward compatibility için eski method'u koruyoruz
    public String login(String email, String password) {
        LoginRequestDto request = new LoginRequestDto(email, password);
        return login(request).token();
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        // Email uniqueness kontrolü
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }

        // Role kontrolü - sadece ADMIN başka ADMIN oluşturabilir
        if (request.role() == Role.SUPER_ADMIN) {
            try {
                Long currentUserId = SecurityUtil.currentUserId();
                User currentUser = userRepository.findById(currentUserId)
                        .orElseThrow(() -> new EntityNotFoundException("Mevcut kullanıcı bulunamadı"));
                
                if (currentUser.getRole() != Role.SUPER_ADMIN) {
                    throw new IllegalArgumentException("Sadece ADMIN kullanıcıları başka ADMIN oluşturabilir");
                }
            } catch (Exception e) {
                // Eğer kimlik doğrulama yoksa (ilk kullanıcı kaydı), REP rolü ata
                if (userRepository.count() == 0) {
                    // İlk kullanıcı ADMIN olabilir
                } else {
                    throw new IllegalArgumentException("ADMIN oluşturmak için yetkiniz yok");
                }
            }
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.DEALER_USER)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        
        return new AuthResponseDto(token, UserMapper.toResponseDto(savedUser));
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {
        Long currentUserId = SecurityUtil.currentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mevcut şifre hatalı");
        }

        // Yeni şifre aynı olmamalı
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Yeni şifre mevcut şifre ile aynı olamaz");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public AuthResponseDto refreshToken(String token) {
        // Bearer prefix'ini kaldır
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Token'dan kullanıcı ID'sini al
        String userIdStr = jwtService.extractSubject(token);
        Long userId = Long.parseLong(userIdStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        // Yeni token oluştur
        String newToken = jwtService.generateToken(user);
        return new AuthResponseDto(newToken, UserMapper.toResponseDto(user));
    }

    public void logout() {
        // JWT stateless olduğu için logout işlemi client-side'da token'ı silmekle yapılır
        // Burada isteğe bağlı olarak blacklist mantığı eklenebilir
        // Şimdilik boş bırakıyoruz
    }

    @Transactional
    public PasswordResetResponseDto forgotPassword(ForgotPasswordRequestDto request) {
        // Kullanıcıyı email ile bul
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bu email adresi ile kayıtlı kullanıcı bulunamadı"));

        // Önceki token'ları geçersiz kıl
        passwordResetTokenRepository.invalidateUserTokens(user.getId());

        // Yeni token oluştur
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1 saat geçerli

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Email gönder
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);

        return new PasswordResetResponseDto("Şifre sıfırlama linki email adresinize gönderildi", true);
    }

    @Transactional
    public PasswordResetResponseDto resetPassword(ResetPasswordRequestDto request) {
        // Token'ı bul ve doğrula
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(request.token(), LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz veya süresi dolmuş token"));

        // Token'ı kullanıldı olarak işaretle
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Kullanıcının şifresini güncelle
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new PasswordResetResponseDto("Şifre başarıyla güncellendi", true);
    }
}

