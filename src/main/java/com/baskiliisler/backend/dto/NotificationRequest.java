package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    private NotificationType type;
    private NotificationPriority priority;
    private String title;
    private String message;
    private Long targetUserId;
    private Long factoryId;
    private String entityType;
    private Long entityId;
    private Map<String, Object> metadata;
    
    // Convenience methods
    public static NotificationRequestBuilder builder() {
        return new NotificationRequestBuilder();
    }
    
    public static class NotificationRequestBuilder {
        private NotificationRequestBuilder() {}
        
        public NotificationRequestBuilder critical() {
            this.priority = NotificationPriority.CRITICAL;
            return this;
        }
        
        public NotificationRequestBuilder high() {
            this.priority = NotificationPriority.HIGH;
            return this;
        }
        
        public NotificationRequestBuilder medium() {
            this.priority = NotificationPriority.MEDIUM;
            return this;
        }
        
        public NotificationRequestBuilder low() {
            this.priority = NotificationPriority.LOW;
            return this;
        }
        
        public NotificationRequestBuilder forFactory(Long factoryId) {
            this.factoryId = factoryId;
            return this;
        }
        
        public NotificationRequestBuilder forUser(Long userId) {
            this.targetUserId = userId;
            return this;
        }
        
        public NotificationRequestBuilder relatedTo(String entityType, Long entityId) {
            this.entityType = entityType;
            this.entityId = entityId;
            return this;
        }
        
        public NotificationRequest build() {
            NotificationRequest request = new NotificationRequest();
            request.type = this.type;
            request.priority = this.priority;
            request.title = this.title;
            request.message = this.message;
            request.targetUserId = this.targetUserId;
            request.factoryId = this.factoryId;
            request.entityType = this.entityType;
            request.entityId = this.entityId;
            request.metadata = this.metadata;
            return request;
        }
    }
}
