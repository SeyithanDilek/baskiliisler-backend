package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.NotificationResponseDto;
import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import com.baskiliisler.backend.config.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Notification mockNotification;
    private NotificationResponseDto mockResponseDto;

    @BeforeEach
    void setUp() {
        mockNotification = Notification.builder()
                .id(1L)
                .type(NotificationType.NEW_ORDER)
                .priority(NotificationPriority.CRITICAL)
                .title("Yeni Sipariş")
                .message("Test mesajı")
                .targetUserId(1L)
                .factoryId(null)
                .entityType("ORDER")
                .entityId(123L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"test\":\"value\"}")
                .build();

        mockResponseDto = NotificationResponseDto.builder()
                .id(1L)
                .type(NotificationType.NEW_ORDER)
                .priority(NotificationPriority.CRITICAL)
                .title("Yeni Sipariş")
                .message("Test mesajı")
                .targetUserId(1L)
                .factoryId(null)
                .entityType("ORDER")
                .entityId(123L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(Map.of("test", "value"))
                .build();
    }

    @Test
    void getAllNotifications_ShouldReturnNotifications() {
        // Given
        List<NotificationResponseDto> notifications = List.of(mockResponseDto);
        when(notificationService.getAllNotifications(1L, 0, 20)).thenReturn(notifications);

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<List<NotificationResponseDto>> response = notificationController.getAllNotifications(0, 20);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            
            NotificationResponseDto dto = response.getBody().get(0);
            assertEquals(1L, dto.getId());
            assertEquals(NotificationType.NEW_ORDER, dto.getType());
            assertEquals(NotificationPriority.CRITICAL, dto.getPriority());
            assertEquals("Yeni Sipariş", dto.getTitle());
            assertEquals("Test mesajı", dto.getMessage());
            assertEquals("ORDER", dto.getEntityType());
            assertEquals(123L, dto.getEntityId());
            assertEquals(false, dto.getIsRead());
            
            verify(notificationService, times(1)).getAllNotifications(1L, 0, 20);
        }
    }

    @Test
    void getUnreadNotifications_ShouldReturnUnreadNotifications() {
        // Given
        List<NotificationResponseDto> notifications = List.of(mockResponseDto);
        when(notificationService.getUnreadNotifications(1L)).thenReturn(notifications);

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<List<NotificationResponseDto>> response = notificationController.getUnreadNotifications();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(false, response.getBody().get(0).getIsRead());
            
            verify(notificationService, times(1)).getUnreadNotifications(1L);
        }
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        // Given
        List<NotificationResponseDto> unreadNotifications = List.of(mockResponseDto, mockResponseDto);
        when(notificationService.getUnreadNotifications(1L)).thenReturn(unreadNotifications);

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<Long> response = notificationController.getUnreadCount();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2L, response.getBody());
            
            verify(notificationService, times(1)).getUnreadNotifications(1L);
        }
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Given
        Long notificationId = 1L;

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<Void> response = notificationController.markAsRead(notificationId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            
            verify(notificationService, times(1)).markAsRead(notificationId, 1L);
        }
    }

    @Test
    void getAllNotifications_WithCustomPageAndSize_ShouldReturnNotifications() {
        // Given
        List<NotificationResponseDto> notifications = List.of(mockResponseDto);
        when(notificationService.getAllNotifications(1L, 1, 10)).thenReturn(notifications);

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<List<NotificationResponseDto>> response = notificationController.getAllNotifications(1, 10);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            
            verify(notificationService, times(1)).getAllNotifications(1L, 1, 10);
        }
    }

    @Test
    void getAllNotifications_WithDefaultParameters_ShouldUseDefaults() {
        // Given
        List<NotificationResponseDto> notifications = List.of(mockResponseDto);
        when(notificationService.getAllNotifications(1L, 0, 20)).thenReturn(notifications);

        // When
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::currentUserId).thenReturn(1L);
            
            ResponseEntity<List<NotificationResponseDto>> response = notificationController.getAllNotifications(0, 20);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            
            verify(notificationService, times(1)).getAllNotifications(1L, 0, 20);
        }
    }
}