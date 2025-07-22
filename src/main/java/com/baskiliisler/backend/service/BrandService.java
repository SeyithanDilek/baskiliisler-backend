package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.config.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final NotificationService notificationService;
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Transactional
    public Brand createBrand(BrandRequestDto dto) {

        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }

        brandRepository.findByName(dto.name())
                .ifPresent(b -> { throw new IllegalArgumentException("Marka zaten var"); });

        // Giriş yapan kullanıcıyı assignedUser olarak ata
        Long userId = SecurityUtil.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));

        Brand brand = BrandMapper.toEntity(dto);
        brand.setAssignedUser(user);
        brand = brandRepository.save(brand);
        
        BrandProcess process = brandProcessService.createBrandProcess(brand);
        
        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                process,
                ProcessStatus.INIT,
                null,
                "{\"brandId\":" + brand.getId() + "}");

        // Notification gönder - hata durumunda ana işlem devam etsin
        try {
            notificationService.notifyNewBrand(brand);
        } catch (Exception e) {
            log.warn("Notification gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        return brand;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public BrandDetailDto findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
    }

    @Transactional
    public BrandDetailDto updateBrand(Long id, BrandUpdateDto dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        if (dto.name() != null && !dto.name().equals(brand.getName()) &&
                brandRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Bu isim zaten kullanımda");
        }

        BrandMapper.updateEntity(dto, brand);
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        
        // Brand process kontrolü
        if (brandProcessService.existsBrandProcess(id)) {
            ProcessStatus currentStatus = brandProcessService.getProcessStatus(id);
            
            // Null kontrolü
            if (currentStatus == null) {
                throw new IllegalStateException("Brand process durumu belirlenemedi.");
            }
            
            // Sadece SAMPLE_LEFT durumunda silinebilir
            if (currentStatus != ProcessStatus.SAMPLE_LEFT) {
                throw new IllegalStateException(
                    "Marka sadece numune bırakıldı (SAMPLE_LEFT) durumunda silinebilir. " +
                    "Mevcut durum: " + currentStatus.name()
                );
            }
            
            log.info("SAMPLE_LEFT durumunda marka siliniyor: {} (ID: {})", brand.getName(), id);
            
            // Güvenli silme: önce bağlı entity'leri sil
            // 1. ProcessHistory'leri sil
            BrandProcess brandProcess = brandProcessService.getBrandProcess(id);
            brandProcessHistoryService.deleteProcessHistoryByProcessId(brandProcess.getId());
            
            // 2. BrandProcess'i sil  
            brandProcessService.deleteBrandProcess(id);
        }
        
        // 3. Son olarak Brand'i sil
        brandRepository.deleteById(id);
        log.info("Brand başarıyla silindi: {} (ID: {})", brand.getName(), id);
    }
}
