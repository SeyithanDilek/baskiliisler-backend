package com.baskiliisler.backend.notification.type;

public enum NotificationPriority {
    CRITICAL("Kritik", "red", 1),
    IMPORTANT("Önemli", "orange", 2),
    NORMAL("Normal", "blue", 3);
    
    private final String displayName;
    private final String color;
    private final int priority;
    
    NotificationPriority(String displayName, String color, int priority) {
        this.displayName = displayName;
        this.color = color;
        this.priority = priority;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean isHigherThan(NotificationPriority other) {
        return this.priority < other.priority;
    }
} 