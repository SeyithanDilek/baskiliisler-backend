package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.NotificationResponseDto;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.config.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bildirimler", description = "Bildirim yönetimi endpoint'leri")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    @Operation(summary = "Okunmamış bildirimleri getir", description = "Kullanıcının okunmamış bildirimlerini listeler")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications() {
        try {
            Long userId = SecurityUtil.currentUserId();
            List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Okunmamış bildirimler getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @Operation(summary = "Tüm bildirimleri getir", description = "Kullanıcının tüm bildirimlerini sayfalı olarak getirir")
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = SecurityUtil.currentUserId();
            List<NotificationResponseDto> notifications = notificationService.getAllNotifications(userId, page, size);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Bildirimler getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Bildirimi okundu olarak işaretle", description = "Belirtilen bildirimi okundu olarak işaretler")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        try {
            Long userId = SecurityUtil.currentUserId();
            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Bildirim okundu olarak işaretlenirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count/unread")
    @Operation(summary = "Okunmamış bildirim sayısını getir", description = "Kullanıcının okunmamış bildirim sayısını döndürür")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            Long userId = SecurityUtil.currentUserId();
            List<NotificationResponseDto> unreadNotifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok((long) unreadNotifications.size());
        } catch (Exception e) {
            log.error("Okunmamış bildirim sayısı getirilirken hata: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 