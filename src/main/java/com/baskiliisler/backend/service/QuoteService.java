package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.QuoteCreateDto;
import com.baskiliisler.backend.dto.QuoteUpdateDto;
import com.baskiliisler.backend.dto.ExpiringOffersResponse;
import com.baskiliisler.backend.dto.WeeklyOffersResponse;
import com.baskiliisler.backend.dto.OffersOverviewResponse;
import com.baskiliisler.backend.model.*;
import com.baskiliisler.backend.repository.*;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.type.ProcessStatus;
import com.baskiliisler.backend.type.QuoteStatus;
import com.baskiliisler.backend.notification.service.NotificationService;
import com.baskiliisler.backend.common.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteService {

    private final BrandRepository brandRepo;
    private final QuoteRepository quoteRepo;
    private final QuoteItemService quoteItemService;
    private final BrandProcessService brandProcessService;
    private final BrandProcessHistoryService brandProcessHistoryService;
    private final OrderService orderService;
    private final EntityManager entityManager;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DealerRepository dealerRepository;

    @Transactional
    public Quote createQuote(QuoteCreateDto quoteCreateDto) {

        Brand brand = brandRepo.findById(quoteCreateDto.brandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara teklif oluşturabilir
            if (brand.getAssignedUser() == null || 
                !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markaya teklif oluşturma yetkiniz yok");
            }
        }

        // Dealer ataması - brand'in dealer'ını kullan
        Dealer dealer = brand.getDealer();
        if (dealer == null) {
            throw new IllegalStateException("Brand'in atanmış bir dealer'ı yok");
        }

        Quote quote = quoteRepo.save(Quote.builder()
                .brand(brand)
                .dealer(dealer)
                .status(QuoteStatus.OFFER_SENT)
                .validUntil(quoteCreateDto.validUntil())
                .createdAt(LocalDateTime.now())
                .currency("TRY")
                .totalPrice(BigDecimal.ZERO)
                .build());

        BigDecimal totalPrice = quoteItemService.assembleAndSaveQuoteItems(quote, quoteCreateDto.items());
        quote.setTotalPrice(totalPrice);
        quote = quoteRepo.save(quote);

        BrandProcess brandProcess = brandProcessService.updateBrandProcessStatus(brand.getId(), ProcessStatus.OFFER_SENT);

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.OFFER_SENT,  // toStatus
                ProcessStatus.SAMPLE_LEFT,  // fromStatus
                "{\"quoteId\":" + quote.getId() + "}");

        // Notification gönder - hata durumunda ana işlem devam etsin
        try {
            notificationService.notifyNewQuote(quote);
        } catch (Exception e) {
            log.warn("Notification gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        return quote;
    }

    @Transactional
    public Quote updateQuote(Long quoteId,QuoteUpdateDto quoteUpdateDto) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri güncelleyebilir
            if (quote.getBrand().getAssignedUser() == null || 
                !quote.getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu teklifi güncelleme yetkiniz yok");
            }
        }

        if (quote.getStatus() != QuoteStatus.DRAFT &&
                quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Bu teklif güncellenemez");
        }

        quoteItemService.deleteQuoteItems(quote.getItems());
        quote.getItems().clear();
        BigDecimal total = quoteItemService.assembleAndSaveQuoteItems(quote, quoteUpdateDto.items());

        if (quoteUpdateDto.validUntil() != null) {
            quote.setValidUntil(quoteUpdateDto.validUntil());
        }
        quote.setTotalPrice(total);
        quote.setUpdatedAt(LocalDateTime.now());
        quote.setStatus(QuoteStatus.OFFER_SENT);

        BrandProcess brandProcess = brandProcessService.updateBrandProcessStatus(quote.getBrand().getId(), ProcessStatus.OFFER_SENT);

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.OFFER_SENT,  // toStatus
                ProcessStatus.OFFER_SENT,  // fromStatus
                "Revizyon yapıldı. Teklif ID: " + quote.getId());
        
        return quote;
    }

    @Transactional
    public Order acceptQuote(Long quoteId,
                             Map<Long, LocalDate> deadlines,
                             String customerLogoUrl,
                             String description,
                             String customerTaxNumber) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri kabul edebilir
            if (quote.getBrand().getAssignedUser() == null || 
                !quote.getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu teklifi kabul etme yetkiniz yok");
            }
        }

        if (quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Quote cannot be accepted");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setUpdatedAt(LocalDateTime.now());

        Order order = orderService.createOrderFromQuote(quote, deadlines, customerLogoUrl, description, customerTaxNumber);
        BrandProcess savedBrandProcess = brandProcessService.updateBrandProcessStatus(quote.getBrand().getId(), ProcessStatus.OFFER_SENT);
        brandProcessHistoryService.saveProcessHistoryForChangeStatus(savedBrandProcess,
                ProcessStatus.ORDER_PLACED,  // toStatus
                ProcessStatus.OFFER_SENT,    // fromStatus
                "Teklif kabul edildi. Sipariş ID: " + order.getId());
        
        // Notification gönder - hata durumunda ana işlem devam etsin
        try {
            notificationService.notifyQuoteAccepted(quote, order);
        } catch (Exception e) {
            log.warn("Notification gönderilirken hata oluştu: {}", e.getMessage());
        }
        
        return order;
    }

    @Transactional
    public void expireQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        if (quote.getStatus() != QuoteStatus.OFFER_SENT) {
            throw new IllegalStateException("Teklif süresi dolmuş");
        }

        quote.setStatus(QuoteStatus.EXPIRED);
        quote.setUpdatedAt(LocalDateTime.now());

        BrandProcess brandProcess = brandProcessService.checkForExpired(quote.getBrand().getId());

        brandProcessHistoryService.saveProcessHistoryForChangeStatus(
                brandProcess,
                ProcessStatus.EXPIRED,     // toStatus
                ProcessStatus.OFFER_SENT,  // fromStatus  
                "Teklif süresi doldu. Teklif ID: " + quote.getId());
    }

    public List<Quote> getAllQuotes() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm teklifleri görebilir
            return quoteRepo.findAllWithBrand();
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri görebilir
            return quoteRepo.findByBrandAssignedUser(currentUser);
        }
    }

    public Quote getQuoteById(Long quoteId) {
        Quote quote = quoteRepo.findByIdWithBrandAndItems(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri görebilir
            if (quote.getBrand().getAssignedUser() == null || 
                !quote.getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu teklife erişim yetkiniz yok");
            }
        }
        
        return quote;
    }

    public List<Quote> getQuotesByBrand(Long brandId) {
        Brand brand = brandRepo.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Marka bulunamadı"));
        
        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri görebilir
            if (brand.getAssignedUser() == null || 
                !brand.getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu markaya ait tekliflere erişim yetkiniz yok");
            }
        }
        
        return quoteRepo.findByBrandWithBrand(brand);
    }

    public List<Quote> getDealerQuotes() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm teklifleri görebilir
            return quoteRepo.findAllWithBrand();
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri görebilir
            return quoteRepo.findByBrandAssignedUser(currentUser);
        }
    }

    public List<Quote> getQuotesByDealer(Long dealerId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın tekliflerini görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            return quoteRepo.findByDealerWithBrand(dealer);
        } else {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri görebilir
            return quoteRepo.findByBrandAssignedUser(currentUser);
        }
    }

    @Transactional
    public void deleteQuote(Long quoteId) {
        Quote quote = quoteRepo.findById(quoteId)
                .orElseThrow(() -> new EntityNotFoundException("Teklif bulunamadı"));

        // Authorization kontrolü
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            // DEALER_USER sadece kendisine assign edilmiş markalara ait teklifleri silebilir
            if (quote.getBrand().getAssignedUser() == null || 
                !quote.getBrand().getAssignedUser().getId().equals(currentUser.getId())) {
                throw new IllegalStateException("Bu teklifi silme yetkiniz yok");
            }
        }

        // Sadece DRAFT, OFFER_SENT veya EXPIRED durumundaki teklifler silinebilir
        if (quote.getStatus() != QuoteStatus.DRAFT && 
            quote.getStatus() != QuoteStatus.OFFER_SENT &&
            quote.getStatus() != QuoteStatus.EXPIRED) {
            throw new IllegalStateException("Bu teklif silinemez. Sadece taslak, gönderilmiş veya süresi dolmuş teklifler silinebilir.");
        }

        quoteRepo.delete(quote);
    }
    
    // Yeni istatistik method'ları
    public ExpiringOffersResponse getExpiringOffers(Long dealerId) {
        User currentUser = userService.getCurrentUser();
        Dealer dealer = getDealerForStatistics(currentUser, dealerId);
        
        // 7 gün içinde geçecek teklifler
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        List<Quote> expiringQuotes = quoteRepo.findByDealerAndValidUntilBeforeAndStatus(
                dealer, expiryDate, QuoteStatus.OFFER_SENT);
        
        List<ExpiringOffersResponse.ExpiringOffer> expiringOffers = expiringQuotes.stream()
                .map(q -> new ExpiringOffersResponse.ExpiringOffer(
                        q.getId(),
                        q.getBrand().getName(),
                        q.getTotalPrice(),
                        q.getValidUntil().atStartOfDay()
                ))
                .toList();
        
        BigDecimal totalValue = expiringQuotes.stream()
                .map(Quote::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new ExpiringOffersResponse(expiringOffers.size(), expiringOffers, totalValue);
    }
    
    public WeeklyOffersResponse getWeeklyOffers(Long dealerId) {
        User currentUser = userService.getCurrentUser();
        Dealer dealer = getDealerForStatistics(currentUser, dealerId);
        
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now();
        
        List<Quote> sentQuotes = quoteRepo.findByDealerAndValidUntilBetweenAndStatus(
                dealer, weekStart, weekEnd, QuoteStatus.OFFER_SENT);
        
        List<Quote> acceptedQuotes = quoteRepo.findByDealerAndValidUntilBetweenAndStatus(
                dealer, weekStart, weekEnd, QuoteStatus.ACCEPTED);
        
        List<Quote> declinedQuotes = quoteRepo.findByDealerAndValidUntilBetweenAndStatus(
                dealer, weekStart, weekEnd, QuoteStatus.DECLINED);
        
        List<Quote> expiredQuotes = quoteRepo.findByDealerAndValidUntilBetweenAndStatus(
                dealer, weekStart, weekEnd, QuoteStatus.EXPIRED);
        
        BigDecimal acceptedValue = acceptedQuotes.stream()
                .map(Quote::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalValue = sentQuotes.stream()
                .map(Quote::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal acceptanceRate = totalValue.compareTo(BigDecimal.ZERO) > 0 ?
                acceptedValue.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
        
        return new WeeklyOffersResponse(
                acceptedQuotes.size(),
                declinedQuotes.size(),
                expiredQuotes.size(),
                declinedQuotes.size() + expiredQuotes.size(),
                sentQuotes.size(),
                acceptedValue,
                totalValue,
                acceptanceRate
        );
    }
    
    public OffersOverviewResponse getOffersOverview(Long dealerId) {
        User currentUser = userService.getCurrentUser();
        Dealer dealer = getDealerForStatistics(currentUser, dealerId);
        
        Object[] totalStats = quoteRepo.countAndSumByDealer(dealer);
        Long totalCount = safeCastToLong(totalStats[0]);
        BigDecimal totalValue = safeCastToBigDecimal(totalStats[1]);
        
        Object[] acceptedStats = quoteRepo.countAndSumByDealerAndStatus(dealer, QuoteStatus.ACCEPTED);
        Long acceptedCount = safeCastToLong(acceptedStats[0]);
        BigDecimal acceptedValue = safeCastToBigDecimal(acceptedStats[1]);
        
        Object[] declinedStats = quoteRepo.countAndSumByDealerAndStatus(dealer, QuoteStatus.DECLINED);
        Long declinedCount = safeCastToLong(declinedStats[0]);
        
        Object[] expiredStats = quoteRepo.countAndSumByDealerAndStatus(dealer, QuoteStatus.EXPIRED);
        Long expiredCount = safeCastToLong(expiredStats[0]);
        
        Object[] pendingStats = quoteRepo.countAndSumByDealerAndStatus(dealer, QuoteStatus.OFFER_SENT);
        Long pendingCount = safeCastToLong(pendingStats[0]);
        
        BigDecimal acceptanceRate = totalValue.compareTo(BigDecimal.ZERO) > 0 ?
                acceptedValue.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
        
        BigDecimal averageOfferValue = totalCount > 0 ?
                totalValue.divide(BigDecimal.valueOf(totalCount), 2, BigDecimal.ROUND_HALF_UP) :
                BigDecimal.ZERO;
        
        return new OffersOverviewResponse(
                totalCount.intValue(),
                pendingCount.intValue(),
                acceptedCount.intValue(),
                declinedCount.intValue(),
                expiredCount.intValue(),
                declinedCount.intValue() + expiredCount.intValue(),
                totalValue,
                acceptedValue,
                acceptanceRate,
                averageOfferValue
        );
    }
    
    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    private Dealer getDealerForStatistics(User currentUser, Long dealerId) {
        if (dealerId == null) {
            if (currentUser.getRole() == Role.SUPER_ADMIN && currentUser.getDealer() == null) {
                throw new IllegalArgumentException("SUPER_ADMIN için dealer seçimi zorunludur");
            }
            return currentUser.getDealer();
        } else {
            if (currentUser.getRole() == Role.SUPER_ADMIN) {
                return dealerRepository.findById(dealerId)
                        .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            } else {
                if (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId())) {
                    throw new IllegalStateException("Sadece kendi dealer'ınızın istatistiklerini görüntüleyebilirsiniz");
                }
                return currentUser.getDealer();
            }
        }
    }
    
    /**
     * Güvenli Long cast metodu
     */
    private Long safeCastToLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            log.warn("Cannot cast {} to Long, using 0", obj);
            return 0L;
        }
    }
    
    /**
     * Güvenli BigDecimal cast metodu
     */
    private BigDecimal safeCastToBigDecimal(Object obj) {
        if (obj == null) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return new BigDecimal(obj.toString());
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (NumberFormatException e) {
            log.warn("Cannot cast {} to BigDecimal, using 0", obj);
            return BigDecimal.ZERO;
        }
    }
}
