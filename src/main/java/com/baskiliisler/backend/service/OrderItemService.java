package com.baskiliisler.backend.service;

import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.model.OrderItem;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.repository.OrderItemRepository;
import com.baskiliisler.backend.type.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public void assembleAndSaveOrderItems(Quote quote,
                                          Map<Long, LocalDate> deadlines,
                                          Order order) {
        quote.getItems().forEach(qi -> {
            LocalDate dl = deadlines == null ? null : deadlines.get(qi.getProduct().getId());
            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .product(qi.getProduct())
                    .quantity(qi.getQuantity())
                    .unitPrice(qi.getUnitPrice())
                    .lineTotal(qi.getLineTotal())
                    .plannedDelivery(dl)
                    .status(OrderItemStatus.PENDING)
                    .build());
        });
    }}
