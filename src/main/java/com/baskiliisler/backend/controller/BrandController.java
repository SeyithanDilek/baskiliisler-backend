package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandResponseDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.service.BrandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "Marka yönetimi API'leri")
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponseDto> createBrand(@RequestBody @Valid BrandRequestDto dto) {
        var brand = brandService.createBrand(dto);
        return ResponseEntity.status(201).body(BrandMapper.toDto(brand));
    }

    @GetMapping
    public List<BrandResponseDto> getAllBrands() {
        return brandService.getAllBrands().stream()
                .map(BrandMapper::toDto)
                .collect(toList());
    }

    @GetMapping("/{id}")
    public BrandDetailDto getById(@PathVariable Long id) {
        return brandService.findById(id);
    }

    @PatchMapping("/{id}")
    public BrandDetailDto update(@PathVariable Long id,
                                 @RequestBody @Valid BrandUpdateDto dto) {
        return brandService.updateBrand(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        brandService.deleteBrand(id);
    }
}
