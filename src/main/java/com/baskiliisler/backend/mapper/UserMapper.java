package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {
    
    public static UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
    
    public static User toUser(UserCreateDto dto, String passwordHash, Role role) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .passwordHash(passwordHash)
                .role(role)
                .build();
    }
    
    public static void updateUserFromDto(User user, UserUpdateDto dto) {
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPhoneNumber(dto.phoneNumber());
        if (dto.role() != null) {
            user.setRole(dto.role());
        }
    }
} 