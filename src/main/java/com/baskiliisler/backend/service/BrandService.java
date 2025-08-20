package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.BrandDetailDto;
import com.baskiliisler.backend.dto.BrandRequestDto;
import com.baskiliisler.backend.dto.BrandUpdateDto;
import com.baskiliisler.backend.dto.MostQuotedBrandResponse;
import com.baskiliisler.backend.mapper.BrandMapper;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.BrandProcess;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.service.EmailService;
import com.baskiliisler.backend.dto.NotificationRequest;
import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final NotificationService notificationService;
    private final EmailService emailService;
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
        brand.setCreatedAt(LocalDateTime.now());
        
        // Dealer ataması - dealerId null ise kullanıcının dealer'ını kullan
        if (dto.dealerId() == null) {
            // dealerId null ise kullanıcının dealer'ını kullan
            if (user.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            brand.setDealer(user.getDealer());
        } else {
            // dealerId verilmişse, sadece SUPER_ADMIN farklı dealer seçebilir
            if (user.getRole() == Role.SUPER_ADMIN) {
                Dealer dealer = dealerRepository.findById(dto.dealerId())
                        .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
                brand.setDealer(dealer);
            } else {
                // Diğer roller sadece kendi dealer'larını seçebilir
                if (user.getDealer() == null) {
                    throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
                }
                if (!dto.dealerId().equals(user.getDealer().getId())) {
                    throw new IllegalStateException("Sadece kendi dealer'ınızı seçebilirsiniz");
                }
                brand.setDealer(user.getDealer());
            }
        }
        
        brand = brandRepository.save(brand);
        
        BrandProcess process = brandProcessService.createBrandProcess(brand);
        
        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                process,
                ProcessStatus.INIT,
                null,
                "{\"brandId\":" + brand.getId() + "}");

        // Super admin'lere yeni marka bildirimi gönder - hata durumunda ana işlem devam etsin
        try {
            User currentUser = getCurrentUser();
            notificationService.notifyUsersByRole(Role.SUPER_ADMIN, 
                NotificationRequest.builder()
                    .type(NotificationType.NEW_BRAND)
                    .priority(NotificationPriority.LOW)
                    .title("Yeni Marka Eklendi")
                    .message(String.format("'%s' markası %s bayisinden %s tarafından eklendi", 
                        brand.getName(), 
                        brand.getDealer().getName(), 
                        currentUser.getName()))
                    .entityType("BRAND")
                    .entityId(brand.getId())
                    .build());
        } catch (Exception e) {
            log.warn("Notification gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        // Yeni müşteri hoşgeldiniz maili gönder - hata durumunda ana işlem devam etsin
        try {
            if (brand.getContactEmail() != null && !brand.getContactEmail().isEmpty()) {
                emailService.sendNewCustomerWelcomeEmail(
                    brand.getContactEmail(), 
                    brand.getName(), 
                    brand.getName()
                );
                log.info("Yeni müşteri hoşgeldiniz maili gönderildi: {}", brand.getContactEmail());
            }
        } catch (Exception e) {
            log.warn("Yeni müşteri hoşgeldiniz maili gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        return brand;
    }

    public List<Brand> getAllBrands(Long dealerId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm markaları görebilir
            if (dealerId == null) {
                return brandRepository.findAll();
            } else {
                Dealer dealer = dealerRepository.findById(dealerId)
                        .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
                return brandRepository.findByDealer(dealer);
            }
        } else {
            // DEALER_USER ve diğer roller sadece kendilerine assign edilmiş markaları görebilir
            return brandRepository.findByAssignedUserOrderByUpdatedAtDesc(currentUser);
        }
    }

    public List<Brand> getDealerBrands() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm markaları görebilir
            return brandRepository.findAll();
        } else {
            // DEALER_USER ve diğer roller sadece kendilerine assign edilmiş markaları görebilir
            return brandRepository.findByAssignedUserOrderByUpdatedAtDesc(currentUser);
        }
    }

    public List<Brand> getBrandsByDealer(Long dealerId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın markalarını görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            return brandRepository.findByDealer(dealer);
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markaları görebilir
            return brandRepository.findByAssignedUserOrderByUpdatedAtDesc(currentUser);
        }
    }

    public BrandDetailDto findById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markaları görebilir
            if (brand.getAssignedUser() == null || !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markaya erişim yetkiniz yok");
            }
        }
        
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    public Brand getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markaları görebilir
            if (brand.getAssignedUser() == null || !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markaya erişim yetkiniz yok");
            }
        }
        
        return brand;
    }

    @Transactional
    public BrandDetailDto updateBrand(Long id, BrandUpdateDto dto) {
        var violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException("Geçersiz e-posta formatı");
        }

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markaları güncelleyebilir
            if (brand.getAssignedUser() == null || !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markayı güncelleme yetkiniz yok");
            }
        }

        if (dto.name() != null && !dto.name().equals(brand.getName()) &&
                brandRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Bu isim zaten kullanımda");
        }

        // assignedUserId güncelleme kontrolü
        if (dto.assignedUserId() != null) {
            if (dto.assignedUserId().equals(-1L)) {
                // -1 değeri ile assignedUser'ı kaldır
                brand.setAssignedUser(null);
            } else {
                // Yeni assignedUser'ı set et
                User newAssignedUser = userRepository.findById(dto.assignedUserId())
                        .orElseThrow(() -> new EntityNotFoundException("Atanacak kullanıcı bulunamadı"));
                
                // SUPER_ADMIN değilse, sadece kendi dealer'ına ait kullanıcıları atayabilir
                if (currentUser.getRole() != Role.SUPER_ADMIN) {
                    if (newAssignedUser.getDealer() == null || 
                        !newAssignedUser.getDealer().getId().equals(currentUser.getDealer().getId())) {
                        throw new IllegalStateException("Sadece kendi dealer'ınıza ait kullanıcıları atayabilirsiniz");
                    }
                }
                
                brand.setAssignedUser(newAssignedUser);
            }
        }

        BrandMapper.updateEntity(dto, brand);
        ProcessStatus status = brandProcessService.getProcessStatus(brand.getId());
        return BrandMapper.toDetailDto(brand, status);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markaları silebilir
            if (brand.getAssignedUser() == null || !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markayı silme yetkiniz yok");
            }
        }
        
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

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }

    public MostQuotedBrandResponse getMostQuotedBrand(Long dealerId) {
        User currentUser = getCurrentUser();
        List<MostQuotedBrandResponse> results;

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm istatistikleri görebilir
            if (dealerId == null) {
                results = brandRepository.findMostQuotedBrand();
            } else {
                Dealer dealer = dealerRepository.findById(dealerId)
                        .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
                results = brandRepository.findMostQuotedBrandByDealer(dealer);
            }
        } else {
            // DEALER_USER sadece kendi assign edilmiş markalarının istatistiklerini görebilir
            // Bu durumda dealer bazlı değil, user bazlı istatistik gerekiyor
            // Şimdilik dealer bazlı kullanıyoruz, ileride user bazlı query eklenebilir
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            results = brandRepository.findMostQuotedBrandByDealer(currentUser.getDealer());
        }
        
        return results.isEmpty() ?
                new MostQuotedBrandResponse(null, "Marka bulunamadı", 0, BigDecimal.ZERO) :
                results.get(0);
    }
}
