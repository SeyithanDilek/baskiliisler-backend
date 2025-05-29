package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.mapper.ProductMapper;
import com.baskiliisler.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "🛍️ Product Management", description = "Ürün yönetimi API'leri")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Yeni ürün oluştur", description = "Yeni bir ürün oluşturur")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody @Valid ProductRequestDto dto) {
        var product = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductMapper.toResponseDto(product));
    }

    @GetMapping
    @Operation(summary = "Tüm ürünleri listele", description = "Sistemdeki tüm ürünleri listeler")
    public List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/active")
    @Operation(summary = "Aktif ürünleri listele", description = "Sadece aktif ürünleri listeler")
    public List<ProductResponseDto> getActiveProducts() {
        return productService.getActiveProducts().stream()
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile ürün getir", description = "Belirtilen ID'ye sahip ürünü getirir")
    public ProductResponseDto getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Kod ile ürün getir", description = "Belirtilen koda sahip ürünü getirir")
    public ProductResponseDto getByCode(@PathVariable String code) {
        return productService.findByCode(code);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Ürün güncelle", description = "Belirtilen ID'ye sahip ürünü günceller")
    public ProductResponseDto updateProduct(@PathVariable Long id,
                                          @RequestBody @Valid ProductUpdateDto dto) {
        return productService.updateProduct(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ürün sil", description = "Belirtilen ID'ye sahip ürünü siler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Ürünü pasifleştir", description = "Belirtilen ID'ye sahip ürünü pasifleştirir")
    public ProductResponseDto deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return productService.findById(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ürünü aktifleştir", description = "Belirtilen ID'ye sahip ürünü aktifleştirir")
    public ProductResponseDto activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return productService.findById(id);
    }
} 