package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.model.Quote;

import java.util.Collections;
import java.util.List;

public class QuoteMapper {
    public static QuoteResponseDto toDto(Quote q) {
        if (q == null) {
            return null;
        }
        
        List<QuoteResponseDto.QuoteItemResp> items = q.getItems() != null ? 
            q.getItems().stream()
                .map(i -> new QuoteResponseDto.QuoteItemResp(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTaxRate(),
                        i.getLineTotal(),
                        i.getTaxAmount(),
                        i.getLineTotalWithTax()))
                .toList() : 
            Collections.emptyList();
            
        // AssignedUser bilgisi
        QuoteResponseDto.AssignedUserInfo assignedUser = null;
        if (q.getBrand() != null && q.getBrand().getAssignedUser() != null) {
            var user = q.getBrand().getAssignedUser();
            assignedUser = new QuoteResponseDto.AssignedUserInfo(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber()
            );
        }
        
        return new QuoteResponseDto(q.getId(), q.getStatus(),
                q.getTotalPrice(), q.getValidUntil(), 
                q.getBrand().getName(), q.getCreatedAt(), 
                q.getUpdatedAt(),
                q.getDealer() != null ? q.getDealer().getId() : null, 
                items, assignedUser);
    }
}
