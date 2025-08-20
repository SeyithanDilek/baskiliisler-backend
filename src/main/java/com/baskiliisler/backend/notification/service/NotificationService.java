package com.baskiliisler.backend.notification.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.NotificationRequest;
import com.baskiliisler.backend.dto.NotificationResponseDto;
import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.repository.NotificationRepository;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generic bildirim oluşturma
     */
    @Transactional
    public Notification createNotification(NotificationRequest request) {
        try {
            Notification notification = Notification.builder()
                    .type(request.getType())
                    .priority(request.getPriority())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .targetUserId(request.getTargetUserId())
                    .factoryId(request.getFactoryId())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .metadata(serializeMetadata(request.getMetadata()))
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Bildirim oluşturuldu: {} - {}", savedNotification.getId(), savedNotification.getTitle());
            return savedNotification;
        } catch (Exception e) {
            log.error("Bildirim oluşturulurken hata: {}", e.getMessage(), e);
            // Bildirim oluşturulamazsa sistem akışını etkileme
            return null;
        }
    }

    /**
     * Role bazlı bildirim gönderme
     */
    @Transactional
    public void notifyUsersByRole(Role role, NotificationRequest request) {
        try {
            List<User> users = userRepository.findByRole(role);
            for (User user : users) {
                NotificationRequest userRequest = NotificationRequest.builder()
                        .type(request.getType())
                        .priority(request.getPriority())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .targetUserId(user.getId())
                        .factoryId(request.getFactoryId())
                        .entityType(request.getEntityType())
                        .entityId(request.getEntityId())
                        .metadata(request.getMetadata())
                        .build();
                
                createNotification(userRequest);
            }
            log.info("{} rolündeki {} kullanıcıya bildirim gönderildi", role, users.size());
        } catch (Exception e) {
            log.error("Role bazlı bildirim gönderilirken hata: {}", e.getMessage(), e);
            // Bildirim gönderilemezse sistem akışını etkileme
        }
    }

    /**
     * Fabrika bazlı bildirim gönderme
     */
    @Transactional
    public void notifyFactoryUsers(Long factoryId, NotificationRequest request) {
        try {
            List<User> factoryUsers = userRepository.findByRoleAndFactoryId(Role.FACTORY_USER, factoryId);
            for (User user : factoryUsers) {
                NotificationRequest userRequest = NotificationRequest.builder()
                        .type(request.getType())
                        .priority(request.getPriority())
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .targetUserId(user.getId())
                        .factoryId(factoryId)
                        .entityType(request.getEntityType())
                        .entityId(request.getEntityId())
                        .metadata(request.getMetadata())
                        .build();
                
                createNotification(userRequest);
            }
            log.info("Fabrika {} için {} kullanıcıya bildirim gönderildi", factoryId, factoryUsers.size());
        } catch (Exception e) {
            log.error("Fabrika bazlı bildirim gönderilirken hata: {}", e.getMessage(), e);
            // Bildirim gönderilemezse sistem akışını etkileme
        }
    }

    /**
     * Bildirim okundu olarak işaretleme
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        try {
            notificationRepository.findByIdAndTargetUserId(notificationId, userId)
                    .ifPresent(notification -> {
                        notification.markAsRead();
                        notificationRepository.save(notification);
                        log.info("Bildirim okundu olarak işaretlendi: {}", notificationId);
                    });
        } catch (Exception e) {
            log.error("Bildirim okundu olarak işaretlenirken hata: {}", e.getMessage(), e);
        }
    }

    /**
     * Kullanıcının okunmamış bildirimlerini getirme
     */
    public List<NotificationResponseDto> getUnreadNotifications(Long userId) {
        try {
            List<Notification> notifications = notificationRepository.findByTargetUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
            return notifications.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Okunmamış bildirimler getirilirken hata: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Kullanıcının tüm bildirimlerini getirme
     */
    public List<NotificationResponseDto> getAllNotifications(Long userId, int page, int size) {
        try {
            List<Notification> notifications = notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
            // Manual pagination
            int start = page * size;
            int end = Math.min(start + size, notifications.size());
            if (start >= notifications.size()) {
                return List.of();
            }
            return notifications.subList(start, end).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Bildirimler getirilirken hata: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Aylık temizlik - her ayın 1'i saat 00:00'da
     */
    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            long deletedCount = notificationRepository.deleteByCreatedAtBefore(oneMonthAgo);
            log.info("{} adet eski bildirim temizlendi", deletedCount);
        } catch (Exception e) {
            log.error("Eski bildirimler temizlenirken hata: {}", e.getMessage(), e);
        }
    }

    /**
     * Metadata serialization
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Metadata serialization hatası: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Entity to DTO mapping
     */
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        try {
            Map<String, Object> metadata = null;
            if (notification.getMetadata() != null) {
                metadata = objectMapper.readValue(notification.getMetadata(), Map.class);
            }

            return NotificationResponseDto.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .priority(notification.getPriority())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .targetUserId(notification.getTargetUserId())
                    .factoryId(notification.getFactoryId())
                    .entityType(notification.getEntityType())
                    .entityId(notification.getEntityId())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .readAt(notification.getReadAt())
                    .metadata(metadata)
                    .build();
        } catch (Exception e) {
            log.error("Notification DTO mapping hatası: {}", e.getMessage(), e);
            return null;
        }
    }
} 