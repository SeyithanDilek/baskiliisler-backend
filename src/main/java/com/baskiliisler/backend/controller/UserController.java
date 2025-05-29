package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.mapper.UserMapper;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "👥 User Management", description = "Kullanıcı yönetimi API'leri")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mevcut kullanıcı bilgilerini getir", description = "Giriş yapmış kullanıcının bilgilerini getirir")
    public UserResponseDto getCurrentUser() {
        User user = userService.getCurrentUser();
        return UserMapper.toResponseDto(user);
    }

    @PatchMapping("/me")
    @Operation(summary = "Kendi bilgilerini güncelle", description = "Giriş yapmış kullanıcının kendi bilgilerini günceller")
    public UserResponseDto updateCurrentUser(@RequestBody @Valid UserUpdateDto dto) {
        return userService.updateCurrentUser(dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tüm kullanıcıları listele", description = "Sistemdeki tüm kullanıcıları listeler (Sadece ADMIN)")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "ID ile kullanıcı getir", description = "Belirtilen ID'ye sahip kullanıcıyı getirir (Sadece ADMIN)")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcı güncelle", description = "Belirtilen ID'ye sahip kullanıcıyı günceller (Sadece ADMIN)")
    public UserResponseDto updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Kullanıcı sil", description = "Belirtilen ID'ye sahip kullanıcıyı siler (Sadece ADMIN)")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
} 