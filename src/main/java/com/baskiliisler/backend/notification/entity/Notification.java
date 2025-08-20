package com.baskiliisler.backend.notification.entity;

import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.LOW;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;
    
    @Column(name = "factory_id")
    private Long factoryId;
    
    @Column(name = "entity_type", length = 50)
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public boolean isCritical() {
        return this.priority == NotificationPriority.CRITICAL;
    }
    
    public boolean isHigh() {
        return this.priority == NotificationPriority.HIGH;
    }
    
    public boolean isMedium() {
        return this.priority == NotificationPriority.MEDIUM;
    }
    
    public boolean isLow() {
        return this.priority == NotificationPriority.LOW;
    }
} 