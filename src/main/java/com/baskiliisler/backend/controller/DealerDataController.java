package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.BrandResponseDto;
import com.baskiliisler.backend.dto.OrderResponseDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.QuoteResponseDto;
import com.baskiliisler.backend.dto.UserResponseDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.mapper.OrderMapper;
import com.baskiliisler.backend.mapper.ProductMapper;
import com.baskiliisler.backend.mapper.QuoteMapper;
import com.baskiliisler.backend.mapper.UserMapper;
import com.baskiliisler.backend.service.BrandService;
import com.baskiliisler.backend.service.OrderService;
import com.baskiliisler.backend.service.ProductService;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dealer-data")
@RequiredArgsConstructor
@Tag(name = "🏢 Dealer Data Management", description = "DEALER_ADMIN kullanıcıları için bayi verileri API'leri")
public class DealerDataController {

    private final QuoteService quoteService;
    private final ProductService productService;
    private final BrandService brandService;
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/quotes")
    @Operation(summary = "Bayinin tekliflerini getir", description = "DEALER_ADMIN kullanıcısının bayisinin tüm tekliflerini listeler. Opsiyonel dealerId parametresi ile belirli dealer'ın tekliflerini de getirebilir.")
    public List<QuoteResponseDto> getDealerQuotes(@RequestParam(required = false) Long dealerId) {
        if (dealerId != null) {
            return quoteService.getQuotesByDealer(dealerId).stream()
                    .map(QuoteMapper::toDto)
                    .toList();
        }
        return quoteService.getDealerQuotes().stream()
                .map(QuoteMapper::toDto)
                .toList();
    }

    @GetMapping("/products")
    @Operation(summary = "Bayinin ürünlerini getir", description = "DEALER_ADMIN kullanıcısının bayisinin tüm ürünlerini listeler. Opsiyonel dealerId parametresi ile belirli dealer'ın ürünlerini de getirebilir.")
    public List<ProductResponseDto> getDealerProducts(@RequestParam(required = false) Long dealerId) {
        if (dealerId != null) {
            return productService.getProductsByDealer(dealerId).stream()
                    .map(ProductMapper::toResponseDto)
                    .toList();
        }
        return productService.getDealerProducts().stream()
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/brands")
    @Operation(summary = "Bayinin müşterilerini getir", description = "DEALER_ADMIN kullanıcısının bayisinin tüm müşterilerini listeler. Opsiyonel dealerId parametresi ile belirli dealer'ın müşterilerini de getirebilir.")
    public List<BrandResponseDto> getDealerBrands(@RequestParam(required = false) Long dealerId) {
        if (dealerId != null) {
            return brandService.getBrandsByDealer(dealerId).stream()
                    .map(BrandMapper::toDto)
                    .toList();
        }
        return brandService.getDealerBrands().stream()
                .map(BrandMapper::toDto)
                .toList();
    }

    @GetMapping("/orders")
    @Operation(summary = "Bayinin siparişlerini getir", description = "DEALER_ADMIN kullanıcısının bayisinin tüm siparişlerini listeler. Opsiyonel dealerId parametresi ile belirli dealer'ın siparişlerini de getirebilir.")
    public List<OrderResponseDto> getDealerOrders(@RequestParam(required = false) Long dealerId) {
        if (dealerId != null) {
            return orderService.getOrdersByDealer(dealerId).stream()
                    .map(OrderMapper::toDto)
                    .toList();
        }
        return orderService.getDealerOrders().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @GetMapping("/users")
    @Operation(summary = "Bayinin kullanıcılarını getir", description = "DEALER_ADMIN kullanıcısının bayisinin tüm kullanıcılarını listeler. Opsiyonel dealerId parametresi ile belirli dealer'ın kullanıcılarını de getirebilir.")
    public List<UserResponseDto> getDealerUsers(@RequestParam(required = false) Long dealerId) {
        if (dealerId != null) {
            return userService.getUsersByDealer(dealerId).stream()
                    .map(UserMapper::toResponseDto)
                    .toList();
        }
        return userService.getDealerUsers().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }
} 