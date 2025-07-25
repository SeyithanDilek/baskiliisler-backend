package com.baskiliisler.backend.controller;

import com.baskiliisler.backend.dto.DealerCreateDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.service.DealerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dealers")
@RequiredArgsConstructor
@Slf4j
public class DealerController {
    
    private final DealerService dealerService;
    
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Dealer>> getAllDealers() {
        log.info("Tüm dealer'lar getiriliyor");
        return ResponseEntity.ok(dealerService.getAllDealers());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Dealer> getDealerById(@PathVariable Long id) {
        log.info("Dealer getiriliyor: {}", id);
        return dealerService.getDealerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Dealer> getDealerByCode(@PathVariable String code) {
        log.info("Dealer getiriliyor (code): {}", code);
        return dealerService.getDealerByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/master")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Dealer> getMasterDealer() {
        log.info("Master dealer getiriliyor");
        return dealerService.getMasterDealer()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Dealer> createDealer(@RequestBody DealerCreateDto dealerCreateDto) {
        log.info("Yeni dealer oluşturuluyor: {}", dealerCreateDto.getCode());
        Dealer createdDealer = dealerService.createDealer(dealerCreateDto);
        return ResponseEntity.ok(createdDealer);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Dealer> updateDealer(@PathVariable Long id, @RequestBody Dealer dealer) {
        log.info("Dealer güncelleniyor: {}", id);
        Dealer updatedDealer = dealerService.updateDealer(id, dealer);
        return ResponseEntity.ok(updatedDealer);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        log.info("Dealer siliniyor: {}", id);
        dealerService.deleteDealer(id);
        return ResponseEntity.noContent().build();
    }
} 