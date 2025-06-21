package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.FactoryAssignDto;
import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.dto.OrderStatusUpdateDto;
import com.baskiliisler.backend.dto.QuoteAcceptDto;
import com.baskiliisler.backend.mapper.OrderMapper;
import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.service.OrderService;
import com.baskiliisler.backend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final QuoteService quoteService;
    private final OrderService orderService;

    @PatchMapping("/{id}/accept")
    public OrderResponseDto accept(@PathVariable Long id,
                                   @RequestBody QuoteAcceptDto dto) {
        Order order = quoteService.acceptQuote(id, dto.itemDeadlines());
        return OrderMapper.toDto(order);
    }

    @GetMapping
    public List<OrderResponseDto> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderMapper.toDto(order));
    }

    @GetMapping("/brand/{brandId}")
    public List<OrderResponseDto> getOrdersByBrand(@PathVariable Long brandId) {
        return orderService.getOrdersByBrand(brandId).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(@PathVariable Long id,
                                                         @RequestBody @Valid OrderStatusUpdateDto dto) {
        Order order = orderService.updateOrderStatus(id, dto.status());
        return ResponseEntity.ok(OrderMapper.toDto(order));
    }

    @PatchMapping("/{id}/assign-factory")
    public ResponseEntity<OrderResponseDto> assignFactory(@PathVariable Long id,
                                                          @RequestBody @Valid FactoryAssignDto dto) {
        Order order = orderService.assignFactory(id, dto.factoryId(), dto.deadline());
        return ResponseEntity.ok(OrderMapper.toDto(order));
    }
}
