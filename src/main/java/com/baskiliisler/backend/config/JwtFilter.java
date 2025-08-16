package com.baskiliisler.backend.config;

import com.baskiliisler.backend.repository.UserRepository;
import com.baskiliisler.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("=== JWT FILTER START ===");
        log.info("Request URI: {}", path);
        
        // Public endpoint'leri kontrol et
        if (isPublicEndpoint(path)) {
            log.info("Public endpoint, bypassing JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header: {}", header);
        
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("No Bearer token found, returning UNAUTHORIZED");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = header.substring(7);
        log.info("JWT Token extracted: {}", token);

        try {
            Claims claims = jwtService.parse(token);
            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);
            
            log.info("User ID extracted: {}", userId);
            log.info("Role extracted: {}", role);

            if (userRepo.existsById(userId)) {
                // JWT claims'den dealerId ve factoryId'yi al
                Long dealerId = null;
                Long factoryId = null;
                
                if (claims.get("dealerId") != null) {
                    dealerId = ((Number) claims.get("dealerId")).longValue();
                    log.info("Dealer ID extracted: {}", dealerId);
                }
                if (claims.get("factoryId") != null) {
                    factoryId = ((Number) claims.get("factoryId")).longValue();
                    log.info("Factory ID extracted: {}", factoryId);
                }
                
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(), // String olarak userId
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                
                // Authentication details'e dealerId ve factoryId'yi ekle
                Map<String, Object> authDetails = new HashMap<>();
                if (dealerId != null) {
                    authDetails.put("dealerId", dealerId);
                }
                if (factoryId != null) {
                    authDetails.put("factoryId", factoryId);
                }
                
                if (!authDetails.isEmpty()) {
                    auth.setDetails(authDetails);
                }
                
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Authentication set successfully for user: {}", userId);
                log.info("Current Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                log.warn("User not found with ID: {}", userId);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/login") ||
               path.startsWith("/auth/register") ||
               path.startsWith("/auth/forgot-password") ||
               path.startsWith("/auth/reset-password") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.equals("/actuator/health");
    }
}
