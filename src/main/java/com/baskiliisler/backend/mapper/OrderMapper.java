package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.model.Order;

import java.util.List;

public class OrderMapper {
    public static OrderResponseDto toDto(Order o) {
        List<OrderResponseDto.ItemResp> itemDtos = o.getItems().stream()
                .map(i -> new OrderResponseDto.ItemResp(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal(),
                        i.getPlannedDelivery(),
                        i.getStatus()))
                .toList();

        OrderResponseDto.FactoryInfo fi = null;
        if (o.getFactory() != null) {
            fi = new OrderResponseDto.FactoryInfo(
                    o.getFactory().getId(),
                    o.getFactory().getName());
        }

        return new OrderResponseDto(
                o.getId(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getDeadline(),
                o.getDeliveredAt(),
                o.getTotalPrice(),
                fi,
                itemDtos);
    }

}
