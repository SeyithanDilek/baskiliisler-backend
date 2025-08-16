package com.baskiliisler.backend.config;

import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

@NoArgsConstructor
public final class SecurityUtil {

   
    public static Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Kimlik doğrulanmadı");
        }
        
        Object principal = auth.getPrincipal();
        
        // Eğer principal zaten Long ise (önceki çalışan hali)
        if (principal instanceof Long) {
            return (Long) principal;
        }
        
        // Eğer principal String ise (JWT'den gelen userId)
        if (principal instanceof String) {
            String userIdStr = (String) principal;
            
            // anonymousUser kontrolü
            if ("anonymousUser".equals(userIdStr)) {
                throw new AccessDeniedException("JWT token geçersiz veya eksik. Lütfen tekrar giriş yapın.");
            }
            
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                throw new AccessDeniedException("Geçersiz user ID formatı: " + userIdStr);
            }
        }
        
        throw new AccessDeniedException("Kimlik doğrulanmadı");
    }

    public static Long currentUserDealerId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Kimlik doğrulanmadı");
        }
        
        // Authentication details'den dealerId'yi al
        if (auth.getDetails() instanceof java.util.Map<?, ?> claims) {
            Object dealerIdObj = claims.get("dealerId");
            if (dealerIdObj instanceof Number) {
                return ((Number) dealerIdObj).longValue();
            }
        }
        
        return null;
    }

    public static Long currentUserFactoryId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new AccessDeniedException("Kimlik doğrulanmadı");
        }
        
        // Authentication details'den factoryId'yi al
        if (auth.getDetails() instanceof java.util.Map<?, ?> claims) {
            Object factoryIdObj = claims.get("factoryId");
            if (factoryIdObj instanceof Number) {
                return ((Number) factoryIdObj).longValue();
            }
        }
        
        return null;
    }
}
