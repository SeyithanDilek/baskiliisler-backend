package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.GlobalExceptionHandler;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.dto.UserUpdateDto;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private UserResponseDto testUserResponseDto;
    private UserUpdateDto testUserUpdateDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role(Role.ADMIN)
                .build();

        testUserResponseDto = new UserResponseDto(
                1L,
                "Test User",
                "test@example.com",
                Role.ADMIN
        );

        testUserUpdateDto = new UserUpdateDto(
                "Updated User",
                "updated@example.com",
                Role.ADMIN
        );
    }

    @Nested
    @DisplayName("GET /users/me - Mevcut kullanıcı bilgileri")
    class GetCurrentUser {

        @Test
        @DisplayName("Mevcut kullanıcı bilgileri getirildiğinde 200 OK döndürmeli")
        void whenGetCurrentUser_thenShouldReturn200() throws Exception {
            // Given
            when(userService.getCurrentUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.name").value(testUser.getName()))
                    .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                    .andExpect(jsonPath("$.role").value(testUser.getRole().name()));

            verify(userService).getCurrentUser();
        }

        @Test
        @DisplayName("Kullanıcı bulunamadığında 404 Not Found döndürmeli")
        void givenUserNotFound_whenGetCurrentUser_thenShouldReturn404() throws Exception {
            // Given
            when(userService.getCurrentUser()).thenThrow(new EntityNotFoundException("Kullanıcı bulunamadı"));

            // When & Then
            mockMvc.perform(get("/users/me"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));

            verify(userService).getCurrentUser();
        }
    }

    @Nested
    @DisplayName("PATCH /users/me - Kendi bilgilerini güncelle")
    class UpdateCurrentUser {

        @Test
        @DisplayName("Geçerli verilerle kendi bilgileri güncellendiğinde 200 OK döndürmeli")
        void givenValidData_whenUpdateCurrentUser_thenShouldReturn200() throws Exception {
            // Given
            when(userService.updateCurrentUser(any(UserUpdateDto.class))).thenReturn(testUserResponseDto);

            // When & Then
            mockMvc.perform(patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUserResponseDto.id()))
                    .andExpect(jsonPath("$.name").value(testUserResponseDto.name()))
                    .andExpect(jsonPath("$.email").value(testUserResponseDto.email()));

            verify(userService).updateCurrentUser(any(UserUpdateDto.class));
        }

        @Test
        @DisplayName("Geçersiz verilerle güncelleme yapıldığında 400 Bad Request döndürmeli")
        void givenInvalidData_whenUpdateCurrentUser_thenShouldReturn400() throws Exception {
            // Given
            UserUpdateDto invalidDto = new UserUpdateDto("", "invalid-email", Role.ADMIN);

            // When & Then
            mockMvc.perform(patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).updateCurrentUser(any(UserUpdateDto.class));
        }
    }

    @Nested
    @DisplayName("GET /users - Tüm kullanıcıları listele")
    class GetAllUsers {

        @Test
        @DisplayName("Tüm kullanıcılar listelendiğinde 200 OK döndürmeli")
        void whenGetAllUsers_thenShouldReturn200() throws Exception {
            // Given
            when(userService.getAllUsers()).thenReturn(List.of(testUser));

            // When & Then
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testUser.getId()))
                    .andExpect(jsonPath("$[0].name").value(testUser.getName()));

            verify(userService).getAllUsers();
        }
    }

    @Nested
    @DisplayName("GET /users/{id} - ID ile kullanıcı getir")
    class GetUserById {

        @Test
        @DisplayName("Geçerli ID ile kullanıcı getirildiğinde 200 OK döndürmeli")
        void givenValidId_whenGetUserById_thenShouldReturn200() throws Exception {
            // Given
            when(userService.findById(1L)).thenReturn(testUserResponseDto);

            // When & Then
            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUserResponseDto.id()))
                    .andExpect(jsonPath("$.name").value(testUserResponseDto.name()));

            verify(userService).findById(1L);
        }

        @Test
        @DisplayName("Geçersiz ID ile kullanıcı arandığında 404 Not Found döndürmeli")
        void givenInvalidId_whenGetUserById_thenShouldReturn404() throws Exception {
            // Given
            when(userService.findById(999L)).thenThrow(new EntityNotFoundException("Kullanıcı bulunamadı"));

            // When & Then
            mockMvc.perform(get("/users/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));

            verify(userService).findById(999L);
        }
    }

    @Nested
    @DisplayName("PATCH /users/{id} - Kullanıcı güncelle")
    class UpdateUser {

        @Test
        @DisplayName("Geçerli verilerle kullanıcı güncellendiğinde 200 OK döndürmeli")
        void givenValidData_whenUpdateUser_thenShouldReturn200() throws Exception {
            // Given
            when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(testUserResponseDto);

            // When & Then
            mockMvc.perform(patch("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUserResponseDto.id()));

            verify(userService).updateUser(eq(1L), any(UserUpdateDto.class));
        }

        @Test
        @DisplayName("Geçersiz ID ile güncelleme yapıldığında 404 Not Found döndürmeli")
        void givenInvalidId_whenUpdateUser_thenShouldReturn404() throws Exception {
            // Given
            when(userService.updateUser(eq(999L), any(UserUpdateDto.class)))
                    .thenThrow(new EntityNotFoundException("Kullanıcı bulunamadı"));

            // When & Then
            mockMvc.perform(patch("/users/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));

            verify(userService).updateUser(eq(999L), any(UserUpdateDto.class));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id} - Kullanıcı sil")
    class DeleteUser {

        @Test
        @DisplayName("Mevcut kullanıcı silindiğinde 204 No Content döndürmeli")
        void givenExistingUser_whenDeleteUser_thenShouldReturn204() throws Exception {
            // Given
            doNothing().when(userService).deleteUser(1L);

            // When & Then
            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());

            verify(userService).deleteUser(1L);
        }

        @Test
        @DisplayName("Mevcut olmayan kullanıcı silinmeye çalışıldığında 404 Not Found döndürmeli")
        void givenNonExistingUser_whenDeleteUser_thenShouldReturn404() throws Exception {
            // Given
            doThrow(new EntityNotFoundException("Kullanıcı bulunamadı")).when(userService).deleteUser(999L);

            // When & Then
            mockMvc.perform(delete("/users/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Kullanıcı bulunamadı"));

            verify(userService).deleteUser(999L);
        }

        @Test
        @DisplayName("Kendi hesabını silmeye çalışıldığında 400 Bad Request döndürmeli")
        void givenSelfDeletion_whenDeleteUser_thenShouldReturn400() throws Exception {
            // Given
            doThrow(new IllegalArgumentException("Kendi hesabınızı silemezsiniz")).when(userService).deleteUser(1L);

            // When & Then
            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Kendi hesabınızı silemezsiniz"));

            verify(userService).deleteUser(1L);
        }
    }
} 