package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.dto.DealerCreateDto;
import com.baskiliisler.backend.dto.UserCreateDto;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerService {
    
    private final DealerRepository dealerRepository;
    private final UserService userService;
    private final EmailService emailService;
    
    @Transactional(readOnly = true)
    public List<Dealer> getAllDealers() {
        return dealerRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Dealer> getDealerById(Long id) {
        return dealerRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Dealer> getDealerByCode(String code) {
        return dealerRepository.findByCode(code);
    }
    
    @Transactional(readOnly = true)
    public Optional<Dealer> getMasterDealer() {
        return dealerRepository.findMasterDealer();
    }
    
    @Transactional
    public Dealer createDealer(DealerCreateDto dealerCreateDto) {
        if (dealerRepository.existsByCode(dealerCreateDto.getCode())) {
            throw new IllegalArgumentException("Bu dealer kodu zaten kullanılıyor: " + dealerCreateDto.getCode());
        }
        
        // Dealer'ı oluştur
        Dealer dealer = Dealer.builder()
                .code(dealerCreateDto.getCode())
                .name(dealerCreateDto.getName())
                .master(dealerCreateDto.isMaster())
                .build();
        
        // Dealer'ı kaydet
        Dealer savedDealer = dealerRepository.save(dealer);
        
        // Dealer admin kullanıcısı oluştur
        createDealerAdmin(savedDealer, dealerCreateDto);
        
        log.info("Dealer ve admin kullanıcısı oluşturuldu: {} (ID: {})", savedDealer.getName(), savedDealer.getId());
        
        return savedDealer;
    }
    
    private void createDealerAdmin(Dealer dealer, DealerCreateDto dealerCreateDto) {
        try {
            // Admin bilgilerini al
            String adminName = dealerCreateDto.getAdminName() != null ? 
                    dealerCreateDto.getAdminName() : dealer.getName() + " Admin";
            
            String adminEmail = dealerCreateDto.getAdminEmail() != null ? 
                    dealerCreateDto.getAdminEmail() : "admin@" + dealer.getCode().toLowerCase() + ".com";
            
            String adminPhone = dealerCreateDto.getAdminPhoneNumber() != null ? 
                    dealerCreateDto.getAdminPhoneNumber() : "+90 555 000 00 00";
            
            // UserCreateDto oluştur
            UserCreateDto adminDto = new UserCreateDto(
                adminName,
                adminEmail,
                adminPhone
            );
            
            // Admin kullanıcısını oluştur (DEALER_ADMIN rolü ile ve dealer_id ile)
            userService.createUser(adminDto, Role.DEALER_ADMIN, dealer.getId());
            
            log.info("Dealer admin kullanıcısı oluşturuldu: {} (Dealer: {})", adminEmail, dealer.getName());
            
        } catch (Exception e) {
            log.error("Dealer admin kullanıcısı oluşturulurken hata: {}", e.getMessage());
            throw new RuntimeException("Dealer admin kullanıcısı oluşturulamadı: " + e.getMessage());
        }
    }
    
    @Transactional
    public Dealer updateDealer(Long id, Dealer dealerDetails) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dealer bulunamadı: " + id));
        
        // Master dealer'ın master özelliğini değiştirmeye izin verme
        if (dealer.isMaster() && !dealerDetails.isMaster()) {
            throw new IllegalArgumentException("Master dealer'ın master özelliği değiştirilemez");
        }
        
        dealer.setCode(dealerDetails.getCode());
        dealer.setName(dealerDetails.getName());
        dealer.setMaster(dealerDetails.isMaster());
        
        return dealerRepository.save(dealer);
    }
    
    @Transactional
    public void deleteDealer(Long id) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dealer bulunamadı: " + id));
        
        if (dealer.isMaster()) {
            throw new IllegalArgumentException("Master dealer silinemez");
        }
        
        dealerRepository.delete(dealer);
    }
} 