package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.OrderItemStatus;
import com.baskiliisler.backend.type.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDate deadline,             // üst limit - null olabilir
        LocalDateTime deliveredAt,      // tüm kalemler bitince set edilir
        BigDecimal totalPrice,
        BrandInfo brand,                // hangi markaya ait
        FactoryInfo factory,            // null: henüz atanmadı
        List<ItemResp> items,           // kalem listesi
        String customerLogoUrl,         // müşterinin logosu
        String description,             // sipariş açıklaması
        String customerTaxNumber,       // müşterinin vergi numarası
        List<String> imageUrls          // üretim için gönderilen image URL'leri
) {

    public record BrandInfo(
            Long id, 
            String name, 
            String logoUrl,
            String contactPhone,
            AssignedUserInfo assignedUser) {}
    
    public record FactoryInfo(
            Long id, 
            String name,
            String phone,
            String address) {}
    
    public record AssignedUserInfo(
            Long id,
            String name,
            String email,
            String phone) {}

    public record ItemResp(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal lineTotal,
            BigDecimal taxAmount,
            BigDecimal lineTotalWithTax,
            LocalDate plannedDelivery,  // kalem bazlı teslim hedefi
            OrderItemStatus status      // PENDING / READY / DELIVERED
    ) {}
}