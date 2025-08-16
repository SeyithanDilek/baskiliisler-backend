package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.*;
import com.baskiliisler.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "Kimlik doğrulama işlemleri")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Giriş yap", description = "Email ve şifre ile sisteme giriş yapar")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @Operation(summary = "Kayıt ol", description = "Yeni kullanıcı kaydı oluşturur")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştir", description = "Giriş yapmış kullanıcının şifresini değiştirir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        authService.changePassword(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token yenile", description = "Mevcut token ile yeni token alır")
    public AuthResponseDto refreshToken(@RequestHeader("Authorization") String token) {
        return authService.refreshToken(token);
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış yap", description = "Sistemden güvenli çıkış yapar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        authService.logout();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifremi unuttum", description = "Email adresine şifre sıfırlama linki gönderir")
    public PasswordResetResponseDto forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırla", description = "Token ile yeni şifre belirler")
    public PasswordResetResponseDto resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        return authService.resetPassword(request);
    }

    // Backward compatibility için eski record'ları koruyoruz
    @Deprecated
    public record LoginRequest(String email, String password) {}
    
    @Deprecated
    public record AuthResponse(String token) {}
}
