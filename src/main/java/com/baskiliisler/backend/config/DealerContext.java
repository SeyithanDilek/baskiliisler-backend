package com.baskiliisler.backend.config;

import org.springframework.stereotype.Component;

@Component
public class DealerContext {
    
    private static final ThreadLocal<Long> currentDealerId = new ThreadLocal<>();
    
    public static void setCurrentDealerId(Long dealerId) {
        currentDealerId.set(dealerId);
    }
    
    public static Long getCurrentDealerId() {
        return currentDealerId.get();
    }
    
    public static void clear() {
        currentDealerId.remove();
    }
    
    public static boolean isMasterDealer() {
        Long dealerId = getCurrentDealerId();
        return dealerId != null && dealerId == 1L; // Master dealer ID = 1
    }
} 