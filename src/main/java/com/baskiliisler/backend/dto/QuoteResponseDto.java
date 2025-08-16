package com.baskiliisler.backend.dto;

import com.baskiliisler.backend.type.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteResponseDto(
        Long id,
        QuoteStatus status,
        BigDecimal totalPrice,
        LocalDate validUntil,
        String brandName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long dealerId,
        List<QuoteItemResp> items,
        AssignedUserInfo assignedUser) {

    public record QuoteItemResp(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal taxRate,
            BigDecimal lineTotal,
            BigDecimal taxAmount,
            BigDecimal lineTotalWithTax) {}
    
    public record AssignedUserInfo(
            Long id,
            String name,
            String email,
            String phone) {}
}