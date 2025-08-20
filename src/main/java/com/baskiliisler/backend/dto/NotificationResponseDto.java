package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    
    private Long id;
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private Long targetUserId;
    private Long factoryId;
    private String entityType;
    private Long entityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Map<String, Object> metadata;
    
    // Helper methods
    public boolean isCritical() {
        return NotificationPriority.CRITICAL.equals(this.priority);
    }
    
    public boolean isHigh() {
        return NotificationPriority.HIGH.equals(this.priority);
    }
    
    public boolean isMedium() {
        return NotificationPriority.MEDIUM.equals(this.priority);
    }
    
    public boolean isLow() {
        return NotificationPriority.LOW.equals(this.priority);
    }
} 