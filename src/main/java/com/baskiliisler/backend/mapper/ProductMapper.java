package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.model.Product;

public class ProductMapper {

    public static ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getUnit(),
                product.getUnitPrice(),
                product.isActive()
        );
    }
} 