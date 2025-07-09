package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.NotificationResponseDto;
import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = Notification.builder()
                .id(1L)
                .type(NotificationType.NEW_ORDER)
                .priority(NotificationPriority.CRITICAL)
                .title("Yeni Sipariş")
                .message("Test mesajı")
                .deepLinkUrl("orders/123")
                .entityType("ORDER")
                .entityId(123L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllNotifications_ShouldReturnNotifications() {
        // Given
        List<Notification> notifications = List.of(mockNotification);
        when(notificationService.getAllNotifications()).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponseDto>> response = notificationController.getAllNotifications();

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
        assertEquals("orders/123", dto.getDeepLinkUrl());
        assertEquals("ORDER", dto.getEntityType());
        assertEquals(123L, dto.getEntityId());
        assertEquals(false, dto.getIsRead());
        
        verify(notificationService, times(1)).getAllNotifications();
    }

    @Test
    void getUnreadNotifications_ShouldReturnUnreadNotifications() {
        // Given
        List<Notification> notifications = List.of(mockNotification);
        when(notificationService.getUnreadNotifications()).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponseDto>> response = notificationController.getUnreadNotifications();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(false, response.getBody().get(0).getIsRead());
        
        verify(notificationService, times(1)).getUnreadNotifications();
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        // Given
        long count = 5L;
        when(notificationService.getUnreadCount()).thenReturn(count);

        // When
        ResponseEntity<Long> response = notificationController.getUnreadCount();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody());
        
        verify(notificationService, times(1)).getUnreadCount();
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Given
        Long notificationId = 1L;
        doNothing().when(notificationService).markAsRead(notificationId);

        // When
        ResponseEntity<Void> response = notificationController.markAsRead(notificationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).markAsRead(notificationId);
    }

    @Test
    void markAllAsRead_ShouldMarkAllNotificationsAsRead() {
        // Given
        doNothing().when(notificationService).markAllAsRead();

        // When
        ResponseEntity<Void> response = notificationController.markAllAsRead();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).markAllAsRead();
    }
}