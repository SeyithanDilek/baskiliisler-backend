package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.model.Quote;

public class QuoteMapper {
    public static QuoteResponseDto toDto(Quote q) {
        var items = q.getItems().stream()
            .map(i -> new QuoteResponseDto.QuoteItemResp(
                    i.getProduct().getId(),
                    i.getProduct().getName(),
                    i.getQuantity(),
                    i.getUnitPrice(),
                    i.getLineTotal()))
            .toList();
        return new QuoteResponseDto(q.getId(), q.getStatus(),
                q.getTotalPrice(), q.getValidUntil(), items);
    }
}
