package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.dto.QuoteAcceptDto;
import com.baskiliisler.backend.mapper.OrderMapper;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final QuoteService quoteService;

    @PatchMapping("/{id}/accept")
    public OrderResponseDto accept(@PathVariable Long id,
                                   @RequestBody QuoteAcceptDto dto) {
        Order order = quoteService.acceptQuote(id, dto.itemDeadlines());
        return OrderMapper.toDto(order);
    }

}
