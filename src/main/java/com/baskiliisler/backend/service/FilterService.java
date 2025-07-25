package com.baskiliisler.backend.service;

import com.baskiliisler.backend.config.DealerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final DealerContext dealerContext;
    
    /**
     * Hibernate filter'larını aktif eder
     */
    public void enableFilters() {
        Long dealerId = dealerContext.getCurrentDealerId();
        
        if (dealerId != null) {
            Session session = entityManager.unwrap(Session.class);
            
            // Dealer filter'ını aktif et
            Filter dealerFilter = session.enableFilter("dealerFilter");
            dealerFilter.setParameter("dealerId", dealerId);
            
            log.debug("Dealer filter aktif edildi: dealerId = {}", dealerId);
        } else {
            log.debug("Dealer filter devre dışı (dealerId = null)");
        }
    }
    
    /**
     * Hibernate filter'larını devre dışı bırakır
     */
    public void disableFilters() {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.disableFilter("dealerFilter");
            log.debug("Dealer filter devre dışı bırakıldı");
        } catch (Exception e) {
            log.warn("Filter devre dışı bırakma hatası: {}", e.getMessage());
        }
    }
    
    /**
     * Filter'ları temizler
     */
    public void clearFilters() {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.clear();
            log.debug("Session filter'ları temizlendi");
        } catch (Exception e) {
            log.warn("Filter temizleme hatası: {}", e.getMessage());
        }
    }
} 