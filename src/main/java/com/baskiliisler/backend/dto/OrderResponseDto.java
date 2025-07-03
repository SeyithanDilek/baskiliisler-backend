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
        List<ItemResp> items            // kalem listesi
) {

    public record BrandInfo(Long id, String name) {}
    
    public record FactoryInfo(Long id, String name) {}

    public record ItemResp(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            LocalDate plannedDelivery,  // kalem bazlı teslim hedefi
            OrderItemStatus status      // PENDING / READY / DELIVERED
    ) {}
}