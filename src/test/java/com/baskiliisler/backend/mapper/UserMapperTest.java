package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toResponseDto_ShouldMapUserToResponseDto() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+90 555 123 45 67")
                .passwordHash("hashedPassword")
                .role(Role.DEALER_USER)
                .build();

        // When
        UserResponseDto result = UserMapper.toResponseDto(user);

        // Then
        assertEquals(user.getId(), result.id());
        assertEquals(user.getName(), result.name());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getPhoneNumber(), result.phoneNumber());
        assertEquals(user.getRole(), result.role());
    }

    @Test
    void toUser_ShouldMapCreateDtoToUser() {
        // Given
        UserCreateDto dto = UserCreateDto.builder()
                .name("Test User")
                .email("test@example.com")
                .phoneNumber("+90 555 123 45 67")
                .build();
        String passwordHash = "hashedPassword";
        Role role = Role.DEALER_USER;

        // When
        User result = UserMapper.toUser(dto, passwordHash, role);

        // Then
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(passwordHash, result.getPasswordHash());
        assertEquals(role, result.getRole());
        assertNull(result.getId()); // ID henüz set edilmemiş
    }

    @Test
    void updateUserFromDto_ShouldUpdateUserFields() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .phoneNumber("+90 555 000 00 00")
                .passwordHash("hashedPassword")
                .role(Role.DEALER_USER)
                .build();

        UserUpdateDto dto = new UserUpdateDto(
                "New Name",
                "new@example.com",
                "+90 555 999 99 99",
                Role.SUPER_ADMIN
        );

        // When
        UserMapper.updateUserFromDto(user, dto);

        // Then
        assertEquals(dto.name(), user.getName());
        assertEquals(dto.email(), user.getEmail());
        assertEquals(dto.phoneNumber(), user.getPhoneNumber());
        assertEquals(dto.role(), user.getRole());
        assertEquals(1L, user.getId()); // ID değişmemeli
        assertEquals("hashedPassword", user.getPasswordHash()); // Password değişmemeli
    }
} 