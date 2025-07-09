package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.NotificationResponseDto;
import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Tüm global bildirimleri getir
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications() {
        log.info("Getting all global notifications");
        List<Notification> notifications = notificationService.getAllNotifications();
        List<NotificationResponseDto> response = notifications.stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Okunmamış global bildirimleri getir
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications() {
        log.info("Getting unread global notifications");
        List<Notification> notifications = notificationService.getUnreadNotifications();
        List<NotificationResponseDto> response = notifications.stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Okunmamış global bildirim sayısını getir
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUnreadCount() {
        log.info("Getting unread global notification count");
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Belirli bir bildirimi okundu olarak işaretle
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("Marking notification {} as read", id);
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Tüm bildirimleri okundu olarak işaretle
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marking all global notifications as read");
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    /**
     * Notification entity'sini DTO'ya dönüştür
     */
    private NotificationResponseDto mapToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .priority(notification.getPriority())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .deepLinkUrl(notification.getDeepLinkUrl())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
} 