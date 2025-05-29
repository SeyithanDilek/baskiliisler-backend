package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.model.User;

public class UserMapper {
    
    public static UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
    
    public static void updateUserFromDto(User user, UserUpdateDto dto) {
        user.setName(dto.name());
        user.setEmail(dto.email());
        if (dto.role() != null) {
            user.setRole(dto.role());
        }
    }
} 