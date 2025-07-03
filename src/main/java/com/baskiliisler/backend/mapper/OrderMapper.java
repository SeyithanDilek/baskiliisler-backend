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

        // Brand bilgisi (Order -> Quote -> Brand)
        OrderResponseDto.BrandInfo brandInfo = null;
        if (o.getQuote() != null && o.getQuote().getBrand() != null) {
            brandInfo = new OrderResponseDto.BrandInfo(
                    o.getQuote().getBrand().getId(),
                    o.getQuote().getBrand().getName());
        }

        // Factory bilgisi
        OrderResponseDto.FactoryInfo factoryInfo = null;
        if (o.getFactory() != null) {
            factoryInfo = new OrderResponseDto.FactoryInfo(
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
                brandInfo,
                factoryInfo,
                itemDtos);
    }

}
