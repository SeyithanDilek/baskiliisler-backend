package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.model.Quote;

import java.util.Collections;
import java.util.List;

public class QuoteMapper {
    public static QuoteResponseDto toDto(Quote q) {
        List<QuoteResponseDto.QuoteItemResp> items = q.getItems() != null ? 
            q.getItems().stream()
                .map(i -> new QuoteResponseDto.QuoteItemResp(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()))
                .toList() : 
            Collections.emptyList();
            
        return new QuoteResponseDto(q.getId(), q.getStatus(),
                q.getTotalPrice(), q.getValidUntil(), items);
    }
}
