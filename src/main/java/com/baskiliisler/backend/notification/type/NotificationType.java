package com.baskiliisler.backend.notification.type;

public enum NotificationType {
    // Sipariş notifications
    NEW_ORDER("Yeni Sipariş", "orders"),
    DEADLINE_APPROACHING("Deadline Yaklaşıyor", "orders"),
    DEADLINE_EXCEEDED("Deadline Geçti", "orders"),
    FACTORY_ASSIGNMENT_NEEDED("Fabrika Atama Gerekli", "orders"),
    ORDER_DELIVERED("Sipariş Teslim Edildi", "orders"),
    
    // Teklif notifications
    NEW_QUOTE("Yeni Teklif", "quotes"),
    QUOTE_ACCEPTED("Teklif Kabul Edildi", "quotes"),
    QUOTE_EXPIRING("Teklif Süresi Doluyor", "quotes"),
    QUOTE_EXPIRED("Teklif Süresi Doldu", "quotes"),
    
    // Brand notifications
    NEW_BRAND("Yeni Marka", "brands"),
    
    // Üretim notifications
    PRODUCTION_READY("Üretim Hazır", "orders");
    
    private final String displayName;
    private final String urlPrefix;
    
    NotificationType(String displayName, String urlPrefix) {
        this.displayName = displayName;
        this.urlPrefix = urlPrefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String buildDeepLinkUrl(Long entityId) {
        return urlPrefix + "/" + entityId;
    }
} 