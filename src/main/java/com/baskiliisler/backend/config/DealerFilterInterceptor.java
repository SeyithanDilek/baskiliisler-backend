package com.baskiliisler.backend.config;

import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class DealerFilterInterceptor implements HandlerInterceptor {
    
    private final DealerContext dealerContext;
    private final UserRepository userRepository;
    private final FilterService filterService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse("");
            
            // SUPER_ADMIN için filter'ı devre dışı bırak
            if (role.contains("SUPER_ADMIN")) {
                dealerContext.setCurrentDealerId(null);
                filterService.disableFilters();
                log.debug("SUPER_ADMIN için dealer filter devre dışı");
                return true;
            }
            
            // FACTORY_USER için filter'ı devre dışı bırak
            if (role.contains("FACTORY_USER")) {
                dealerContext.setCurrentDealerId(null);
                filterService.disableFilters();
                log.debug("FACTORY_USER için dealer filter devre dışı");
                return true;
            }
            
            // Diğer roller için dealer_id'yi set et ve filter'ı aktif et
            Long dealerId = getDealerIdFromUser(authentication);
            dealerContext.setCurrentDealerId(dealerId);
            filterService.enableFilters();
            log.debug("Dealer filter aktif: dealerId = {}", dealerId);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // ThreadLocal'ı temizle ve filter'ları temizle
        dealerContext.clear();
        filterService.clearFilters();
    }
    
    private Long getDealerIdFromUser(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElse(null);
            
            if (user != null && user.getDealer() != null) {
                return user.getDealer().getId();
            }
            
            // Eğer user bulunamazsa veya dealer_id yoksa master dealer'ı döndür
            log.warn("User için dealer_id bulunamadı: {}", email);
            return 1L; // Master dealer ID
        } catch (Exception e) {
            log.error("Dealer ID alma hatası: {}", e.getMessage());
            return 1L; // Master dealer ID
        }
    }
} 