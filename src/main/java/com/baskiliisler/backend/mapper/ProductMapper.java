package com.baskiliisler.backend.mapper;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.model.Product;

public class ProductMapper {

    public static ProductResponseDto toResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getUnit(),
                product.getUnitPrice(),
                product.getTaxRate(),
                product.isActive()
        );
    }

    public static Product toEntity(ProductRequestDto dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .unit(dto.unit())
                .unitPrice(dto.unitPrice())
                .taxRate(dto.taxRate())
                .active(true)
                .build();
    }

    public static void updateProductFromDto(Product product, ProductUpdateDto dto) {
        if (dto.name() != null) {
            product.setName(dto.name());
        }
        if (dto.description() != null) {
            product.setDescription(dto.description());
        }
        if (dto.unit() != null) {
            product.setUnit(dto.unit());
        }
        if (dto.unitPrice() != null) {
            product.setUnitPrice(dto.unitPrice());
        }
        if (dto.taxRate() != null) {
            product.setTaxRate(dto.taxRate());
        }
        if (dto.active() != null) {
            product.setActive(dto.active());
        }
    }
} 