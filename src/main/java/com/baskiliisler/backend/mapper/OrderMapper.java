package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.model.Order;

import java.util.List;

public class OrderMapper {
    public static OrderResponseDto toDto(Order o) {
        if (o == null) {
            return null;
        }
        
        List<OrderResponseDto.ItemResp> itemDtos = o.getItems().stream()
                .map(i -> new OrderResponseDto.ItemResp(
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTaxRate(),
                        i.getLineTotal(),
                        i.getTaxAmount(),
                        i.getLineTotalWithTax(),
                        i.getPlannedDelivery(),
                        i.getStatus()))
                .toList();

        // Brand bilgisi (Order -> Quote -> Brand)
        OrderResponseDto.BrandInfo brandInfo = null;
        if (o.getQuote() != null && o.getQuote().getBrand() != null) {
            var brand = o.getQuote().getBrand();
            brandInfo = new OrderResponseDto.BrandInfo(
                    brand.getId(),
                    brand.getName(),
                    brand.getLogoUrl());
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
                itemDtos,
                o.getCustomerLogoUrl(),
                o.getDescription(),
                o.getCustomerTaxNumber());
    }

}
