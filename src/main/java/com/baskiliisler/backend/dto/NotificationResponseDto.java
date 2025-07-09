package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDto {
    private Long id;
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private String deepLinkUrl;
    private String entityType;
    private Long entityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
} 