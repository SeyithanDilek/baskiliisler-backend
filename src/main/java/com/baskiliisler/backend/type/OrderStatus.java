package com.baskiliisler.backend.type;

public enum OrderStatus { 
    PENDING,           // Sipariş alındı
    IN_PRODUCTION,     // Sipariş hazırlanıyor  
    IN_WAREHOUSE,      // Sipariş depoda
    IN_TRANSIT,        // Sipariş yola çıktı
    DELIVERED          // Sipariş teslim edildi
}
