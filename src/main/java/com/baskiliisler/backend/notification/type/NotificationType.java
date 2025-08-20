package com.baskiliisler.backend.notification.type;

public enum NotificationType {
    // Sipariş notifications
    NEW_ORDER("Yeni Sipariş", "orders"),
    DEADLINE_APPROACHING("Teslim Süresi Yaklaşıyor", "orders"),
    FACTORY_ASSIGNMENT_NEEDED("Fabrika Atama Gerekli", "orders"),
    ORDER_DELIVERED("Sipariş Teslim Edildi", "orders"),
    ORDER_CANCELLED("Sipariş İptal Edildi", "orders"),
    
    // Teklif notifications
    NEW_QUOTE("Yeni Teklif", "quotes"),
    QUOTE_STATUS_CHANGED("Teklif Durumu Değişti", "quotes"),
    QUOTE_CONVERTED_TO_ORDER("Teklif Siparişe Dönüştürüldü", "quotes"),
    QUOTE_EXPIRED("Teklif Süresi Doldu", "quotes"),
    
    // Brand notifications
    NEW_BRAND("Yeni Marka", "brands"),
    
    // User notifications
    NEW_USER("Yeni Kullanıcı", "users");
    
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