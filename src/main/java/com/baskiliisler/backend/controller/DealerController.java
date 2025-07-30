package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.service.DealerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dealers")
@RequiredArgsConstructor
@Tag(name = "🏢 Dealer Management", description = "Bayi/Franchise yönetimi API'leri")
public class DealerController {

    private final DealerService dealerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Yeni bayi oluştur", description = "Yeni bir bayi/franchise oluşturur")
    public DealerResponseDto createDealer(@RequestBody @Valid DealerRequestDto dto) {
        return dealerService.createDealer(dto);
    }

    @GetMapping
    @Operation(summary = "Bayileri listele", description = "Kullanıcının yetkisi dahilindeki bayileri listeler")
    public List<DealerResponseDto> getAllDealers() {
        return dealerService.getAllDealers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile bayi getir", description = "Belirtilen ID'ye sahip bayiyi getirir")
    public DealerResponseDto getDealerById(@PathVariable Long id) {
        return dealerService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Bayi güncelle", description = "Belirtilen ID'ye sahip bayiyi günceller")
    public DealerResponseDto updateDealer(@PathVariable Long id, @RequestBody @Valid DealerRequestDto dto) {
        return dealerService.updateDealer(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Bayi sil", description = "Belirtilen ID'ye sahip bayiyi pasif hale getirir")
    public void deleteDealer(@PathVariable Long id) {
        dealerService.deleteDealer(id);
    }
} 