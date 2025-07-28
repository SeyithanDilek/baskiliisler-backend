package com.baskiliisler.backend.notification.service;

import com.baskiliisler.backend.model.Order;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.notification.entity.Notification;
import com.baskiliisler.backend.notification.repository.NotificationRepository;
import com.baskiliisler.backend.notification.type.NotificationType;
import com.baskiliisler.backend.notification.type.NotificationPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    // ================================
    // ASYNC NOTIFICATION CREATORS
    // ================================
    
    @Async("notificationExecutor")
    public void notifyNewOrder(Order order) {
        log.info("Creating notification for new order: {}", order.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.NEW_ORDER)
                .priority(calculateOrderPriority(order))
                .title("Yeni Sipariş Alındı")
                .message(generateOrderMessage(order))
                .deepLinkUrl(NotificationType.NEW_ORDER.buildDeepLinkUrl(order.getId()))
                .entityType("ORDER")
                .entityId(order.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for order: {}", order.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyQuoteAccepted(Quote quote, Order order) {
        log.info("Creating notification for quote accepted: {}", quote.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.QUOTE_ACCEPTED)
                .priority(NotificationPriority.IMPORTANT)
                .title("Teklif Kabul Edildi")
                .message(generateQuoteAcceptedMessage(quote, order))
                .deepLinkUrl(NotificationType.QUOTE_ACCEPTED.buildDeepLinkUrl(order.getId()))
                .entityType("ORDER")
                .entityId(order.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for quote accepted: {}", quote.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyNewQuote(Quote quote) {
        log.info("Creating notification for new quote: {}", quote.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.NEW_QUOTE)
                .priority(NotificationPriority.NORMAL)
                .title("Yeni Teklif Oluşturuldu")
                .message(generateNewQuoteMessage(quote))
                .deepLinkUrl(NotificationType.NEW_QUOTE.buildDeepLinkUrl(quote.getId()))
                .entityType("QUOTE")
                .entityId(quote.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for new quote: {}", quote.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyNewBrand(Brand brand) {
        log.info("Creating notification for new brand: {}", brand.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.NEW_BRAND)
                .priority(NotificationPriority.NORMAL)
                .title("Yeni Marka Kaydı")
                .message(generateNewBrandMessage(brand))
                .deepLinkUrl(NotificationType.NEW_BRAND.buildDeepLinkUrl(brand.getId()))
                .entityType("BRAND")
                .entityId(brand.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for new brand: {}", brand.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyDeadlineApproaching(Order order) {
        log.info("Creating notification for deadline approaching: {}", order.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.DEADLINE_APPROACHING)
                .priority(NotificationPriority.IMPORTANT)
                .title("Deadline Yaklaşıyor")
                .message(generateDeadlineApproachingMessage(order))
                .deepLinkUrl(NotificationType.DEADLINE_APPROACHING.buildDeepLinkUrl(order.getId()))
                .entityType("ORDER")
                .entityId(order.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for deadline approaching: {}", order.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyDeadlineExceeded(Order order) {
        log.info("Creating notification for deadline exceeded: {}", order.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.DEADLINE_EXCEEDED)
                .priority(NotificationPriority.CRITICAL)
                .title("Deadline Geçti!")
                .message(generateDeadlineExceededMessage(order))
                .deepLinkUrl(NotificationType.DEADLINE_EXCEEDED.buildDeepLinkUrl(order.getId()))
                .entityType("ORDER")
                .entityId(order.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for deadline exceeded: {}", order.getId());
    }
    
    @Async("notificationExecutor")
    public void notifyFactoryAssignmentNeeded(Order order) {
        log.info("Creating notification for factory assignment needed: {}", order.getId());
        
        Notification notification = Notification.builder()
                .type(NotificationType.FACTORY_ASSIGNMENT_NEEDED)
                .priority(NotificationPriority.CRITICAL)
                .title("Fabrika Atama Gerekli")
                .message(generateFactoryAssignmentMessage(order))
                .deepLinkUrl(NotificationType.FACTORY_ASSIGNMENT_NEEDED.buildDeepLinkUrl(order.getId()))
                .entityType("ORDER")
                .entityId(order.getId())
                .build();
        
        notificationRepository.save(notification);
        log.info("Notification created for factory assignment needed: {}", order.getId());
    }
    
    // ================================
    // NOTIFICATION MANAGEMENT - GLOBAL
    // ================================
    
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead(LocalDateTime.now());
    }
    
    // ================================
    // HELPER METHODS
    // ================================
    
    private NotificationPriority calculateOrderPriority(Order order) {
        // Büyük tutar siparişleri critical
        if (order.getTotalPrice().compareTo(BigDecimal.valueOf(50000)) > 0) {
            return NotificationPriority.CRITICAL;
        }
        
        // Deadline yakınsa important
        if (order.getDeadline() != null && 
            order.getDeadline().isBefore(LocalDate.now().plusDays(3))) {
            return NotificationPriority.IMPORTANT;
        }
        
        return NotificationPriority.NORMAL;
    }
    
    private String generateOrderMessage(Order order) {
        return String.format("%s markası - %s TL - %s", 
                order.getQuote().getBrand().getName(),
                order.getTotalPrice(),
                order.getDeadline() != null ? 
                    "Deadline: " + order.getDeadline().toString() : 
                    "Deadline belirlenmedi");
    }
    
    private String generateQuoteAcceptedMessage(Quote quote, Order order) {
        return String.format("%s markasından teklif kabul edildi. Sipariş #%d oluşturuldu.", 
                quote.getBrand().getName(), order.getId());
    }
    
    private String generateNewQuoteMessage(Quote quote) {
        return String.format("%s markası için %s TL tutarında teklif oluşturuldu.", 
                quote.getBrand().getName(), quote.getTotalPrice());
    }
    
    private String generateNewBrandMessage(Brand brand) {
        return String.format("Yeni marka kaydı: %s (%s)", 
                brand.getName(), brand.getContactEmail());
    }
    
    private String generateDeadlineApproachingMessage(Order order) {
        return String.format("%s markası siparişi deadline yaklaşıyor (%s)", 
                order.getQuote().getBrand().getName(), 
                order.getDeadline());
    }
    
    private String generateDeadlineExceededMessage(Order order) {
        return String.format("%s markası siparişi deadline geçti! (%s)", 
                order.getQuote().getBrand().getName(), 
                order.getDeadline());
    }
    
    private String generateFactoryAssignmentMessage(Order order) {
        return String.format("%s markası siparişi fabrika atama bekliyor. Tutar: %s TL", 
                order.getQuote().getBrand().getName(), 
                order.getTotalPrice());
    }
} 