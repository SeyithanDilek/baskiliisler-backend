package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandResponseDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.service.BrandService;
import com.baskiliisler.backend.service.CloudinaryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "Marka yönetimi API'leri")
public class BrandController {

    private final BrandService brandService;
    private final CloudinaryService cloudinaryService;

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

    @PatchMapping("/{id}/logo")
    public ResponseEntity<BrandResponseDto> updateLogo(@PathVariable Long id,
                                                       @RequestParam("file") MultipartFile file) {
        try {
            String logoUrl = cloudinaryService.uploadImage(file);
            BrandUpdateDto updateDto = new BrandUpdateDto(null, null, null, null, logoUrl);
            brandService.updateBrand(id, updateDto);
            Brand brand = brandService.getBrandById(id);
            return ResponseEntity.ok(BrandMapper.toDto(brand));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
