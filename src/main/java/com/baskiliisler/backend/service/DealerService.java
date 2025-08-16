package com.baskiliisler.backend.service;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.dto.DealerRequestDto;
import com.baskiliisler.backend.dto.DealerUpdateDto;
import com.baskiliisler.backend.dto.DealerResponseDto;
import com.baskiliisler.backend.dto.DealerOverviewResponse;
import com.baskiliisler.backend.mapper.DealerMapper;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.repository.ProductRepository;
import com.baskiliisler.backend.repository.BrandRepository;
import com.baskiliisler.backend.repository.QuoteRepository;
import com.baskiliisler.backend.repository.OrderRepository;
import com.baskiliisler.backend.type.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealerService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;

    @Transactional
    public DealerResponseDto createDealer(DealerRequestDto dto) {
        // Bayi adı benzersizlik kontrolü
        if (dealerRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Bu bayi adı zaten kullanılıyor");
        }
        
        // Admin email benzersizlik kontrolü
        if (userRepository.existsByEmail(dto.adminEmail())) {
            throw new IllegalArgumentException("Bu email adresi zaten kullanılıyor");
        }
        
        // Dealer oluştur
        Dealer dealer = DealerMapper.toDealer(dto);
        Dealer savedDealer = dealerRepository.save(dealer);
        
        // Dealer admin kullanıcısı oluştur
        String generatedPassword = generateSecurePassword();
        User dealerAdmin = createDealerAdminUser(dto, savedDealer, generatedPassword);
        
        // Email gönder
        try {
            emailService.sendDealerAdminWelcomeEmail(
                dealerAdmin.getEmail(), 
                dealerAdmin.getName(), 
                savedDealer.getName(), 
                generatedPassword
            );
        } catch (Exception e) {
            // Email gönderilemezse kullanıcıyı sil ve hata fırlat
            userRepository.delete(dealerAdmin);
            throw new RuntimeException("Dealer admin oluşturuldu ama email gönderilemedi: " + e.getMessage());
        }
        
        return DealerMapper.toResponseDto(savedDealer, userRepository);
    }

    private User createDealerAdminUser(DealerRequestDto dto, Dealer dealer, String password) {
        User dealerAdmin = User.builder()
                .name(dto.adminName())
                .email(dto.adminEmail())
                .phoneNumber(dto.adminPhoneNumber())
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.DEALER_ADMIN)
                .dealer(dealer)
                .build();
        
        return userRepository.save(dealerAdmin);
    }
    
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        
        return password.toString();
    }

    public List<DealerResponseDto> getAllDealers() {
        User currentUser = getCurrentUser();
        
        // SUPER_ADMIN tüm bayileri görebilir
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return dealerRepository.findByActive(true).stream()
                    .map(dealer -> DealerMapper.toResponseDto(dealer, userRepository))
                    .toList();
        }
        
        // Diğer roller sadece kendi bayilerini görebilir
        if (currentUser.getDealer() == null) {
            throw new IllegalStateException("Kullanıcının atanmış bir bayisi yok");
        }
        
        return List.of(DealerMapper.toResponseDto(currentUser.getDealer(), userRepository));
    }

    public DealerResponseDto findById(Long id) {
        User currentUser = getCurrentUser();
        
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bayi bulunamadı"));
        
        // Yetki kontrolü
        if (currentUser.getRole() != Role.SUPER_ADMIN && 
            !dealer.equals(currentUser.getDealer())) {
            throw new IllegalArgumentException("Bu bayi bilgilerine erişim yetkiniz yok");
        }
        
        return DealerMapper.toResponseDto(dealer, userRepository);
    }

    @Transactional
    public DealerResponseDto updateDealer(Long id, DealerRequestDto dto) {
        User currentUser = getCurrentUser();
        
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bayi bulunamadı"));
        
        // Yetki kontrolü
        if (currentUser.getRole() != Role.SUPER_ADMIN && 
            !dealer.equals(currentUser.getDealer())) {
            throw new IllegalArgumentException("Bu bayi bilgilerini güncelleme yetkiniz yok");
        }
        
        // Bayi adı benzersizlik kontrolü (kendisi hariç)
        if (!dealer.getName().equals(dto.name()) && dealerRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Bu bayi adı zaten kullanılıyor");
        }
        
        DealerMapper.updateDealerFromDto(dealer, dto);
        Dealer savedDealer = dealerRepository.save(dealer);
        
        return DealerMapper.toResponseDto(savedDealer, userRepository);
    }
    
    @Transactional
    public DealerResponseDto updateDealerWithAdmin(Long id, DealerUpdateDto dto) {
        User currentUser = getCurrentUser();
        
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bayi bulunamadı"));
        
        // Yetki kontrolü
        if (currentUser.getRole() != Role.SUPER_ADMIN && 
            !dealer.equals(currentUser.getDealer())) {
            throw new IllegalArgumentException("Bu bayi bilgilerini güncelleme yetkiniz yok");
        }
        
        // Bayi adı benzersizlik kontrolü (kendisi hariç)
        if (!dealer.getName().equals(dto.name()) && dealerRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Bu bayi adı zaten kullanılıyor");
        }
        
        // Dealer bilgilerini güncelle
        DealerMapper.updateDealerFromUpdateDto(dealer, dto);
        Dealer savedDealer = dealerRepository.save(dealer);
        
        return DealerMapper.toResponseDto(savedDealer, userRepository);
    }

    @Transactional
    public void deleteDealer(Long id) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new IllegalArgumentException("Bayi silme yetkiniz yok");
        }
        
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bayi bulunamadı"));
        
        // Bayiye bağlı kullanıcı var mı kontrol et
        long userCount = userRepository.countByDealer(dealer);
        if (userCount > 0) {
            throw new IllegalArgumentException("Bu bayiye bağlı kullanıcılar var. Önce kullanıcıları başka bayilere taşıyın.");
        }
        
        dealer.setActive(false);
        dealerRepository.save(dealer);
    }

    public Dealer getCurrentUserDealer() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            throw new IllegalStateException("SUPER_ADMIN kullanıcısının atanmış bir bayisi yok");
        }
        
        if (currentUser.getDealer() == null) {
            throw new IllegalStateException("Kullanıcının atanmış bir bayisi yok");
        }
        
        return currentUser.getDealer();
    }

    public Long getCurrentUserDealerId() {
        return getCurrentUserDealer().getId();
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }

    public DealerOverviewResponse getDealerOverview(Long dealerId) {
        User currentUser = getCurrentUser();
        
        // Yetki kontrolü
        if (currentUser.getRole() != Role.SUPER_ADMIN && 
            (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId()))) {
            throw new IllegalArgumentException("Bu bayi bilgilerine erişim yetkiniz yok");
        }
        
        // Dealer'ı bul
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Bayi bulunamadı"));
        
        // İstatistikleri hesapla
        Integer totalProducts = productRepository.countByDealer(dealer);
        Integer totalBrands = brandRepository.countByDealer(dealer);
        Integer totalQuotes = quoteRepository.countByDealer(dealer);
        Integer pendingFactoryAssignments = orderRepository.countByDealerAndStatus(dealer, OrderStatus.PENDING);
        
        return new DealerOverviewResponse(
            totalProducts,
            totalBrands,
            totalQuotes,
            pendingFactoryAssignments
        );
    }
} 