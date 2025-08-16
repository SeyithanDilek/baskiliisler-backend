package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.dto.MostOrderedProductResponse;
import com.baskiliisler.backend.mapper.ProductMapper;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.repository.ProductRepository;
import com.baskiliisler.backend.repository.DealerRepository;
import com.baskiliisler.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baskiliisler.backend.config.SecurityUtil;
import com.baskiliisler.backend.common.Role;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    public Product createProduct(ProductRequestDto dto) {
        User currentUser = getCurrentUser();
        
        Product product = ProductMapper.toEntity(dto);
        
        // Dealer ataması
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN için dealer seçimi gerekli
            if (dto.dealerId() == null) {
                throw new IllegalArgumentException("SUPER_ADMIN için dealer seçimi zorunludur");
            }
            Dealer dealer = dealerRepository.findById(dto.dealerId())
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            product.setDealer(dealer);
        } else {
            // Diğer roller için kullanıcının dealer'ı otomatik atanır
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            product.setDealer(currentUser.getDealer());
        }
        
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(Long dealerId) {
        User currentUser = getCurrentUser();
        
        // dealerId parametresi verilmemişse, giriş yapan kullanıcının dealer'ını kullan
        if (dealerId == null) {
            if (currentUser.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN için dealer yoksa tüm ürünleri getir
                if (currentUser.getDealer() == null) {
                    return productRepository.findAll();
                } else {
                    // SUPER_ADMIN'in kendi dealer'ının ürünlerini getir
                    return productRepository.findByDealer(currentUser.getDealer());
                }
            } else {
                // Diğer roller için kullanıcının dealer'ı zorunlu
                if (currentUser.getDealer() == null) {
                    throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
                }
                return productRepository.findByDealer(currentUser.getDealer());
            }
        }
        
        // dealerId parametresi verilmişse
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın ürünlerini görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            return productRepository.findByDealer(dealer);
        } else {
            // Diğer roller sadece kendi dealer'ının ürünlerini görebilir
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            
            // Verilen dealerId kullanıcının kendi dealer'ı değilse hata ver
            if (!dealerId.equals(currentUser.getDealer().getId())) {
                throw new IllegalStateException("Sadece kendi dealer'ınızın ürünlerini görüntüleyebilirsiniz");
            }
            
            return productRepository.findByDealer(currentUser.getDealer());
        }
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + id));
        return ProductMapper.toResponseDto(product);
    }

    public ProductResponseDto updateProduct(Long id, ProductUpdateDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + id));

        ProductMapper.updateProductFromDto(product, dto);
        Product savedProduct = productRepository.save(product);
        return ProductMapper.toResponseDto(savedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Ürün bulunamadı: " + id);
        }
        productRepository.deleteById(id);
    }

    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    public void activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ürün bulunamadı: " + id));
        product.setActive(true);
        productRepository.save(product);
    }

    public List<Product> getDealerProducts() {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN tüm ürünleri görebilir
            return productRepository.findAll();
        } else {
            // DEALER_USER kendi dealer'ının ürünlerini görebilir
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            return productRepository.findByDealer(currentUser.getDealer());
        }
    }

    public List<Product> getProductsByDealer(Long dealerId) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın ürünlerini görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            return productRepository.findByDealer(dealer);
        } else {
            // DEALER_USER kendi dealer'ının ürünlerini görebilir
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            return productRepository.findByDealer(currentUser.getDealer());
        }
    }

    public MostOrderedProductResponse getMostOrderedProduct(Long dealerId) {
        User currentUser = getCurrentUser();
        
        // dealerId parametresi verilmemişse, giriş yapan kullanıcının dealer'ını kullan
        if (dealerId == null) {
            if (currentUser.getRole() == Role.SUPER_ADMIN) {
                // SUPER_ADMIN için dealer yoksa tüm sistemdeki en çok sipariş alan ürünü getir
                if (currentUser.getDealer() == null) {
                    List<MostOrderedProductResponse> results = productRepository.findMostOrderedProduct();
                    return results.isEmpty() ? 
                            new MostOrderedProductResponse(null, "Ürün bulunamadı", 0, BigDecimal.ZERO) :
                            results.get(0);
                } else {
                    // SUPER_ADMIN'in kendi dealer'ının en çok sipariş alan ürününü getir
                    List<MostOrderedProductResponse> results = productRepository.findMostOrderedProductByDealer(currentUser.getDealer());
                    return results.isEmpty() ? 
                            new MostOrderedProductResponse(null, "Ürün bulunamadı", 0, BigDecimal.ZERO) :
                            results.get(0);
                }
            } else {
                // Diğer roller için kullanıcının dealer'ı zorunlu
                if (currentUser.getDealer() == null) {
                    throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
                }
                List<MostOrderedProductResponse> results = productRepository.findMostOrderedProductByDealer(currentUser.getDealer());
                return results.isEmpty() ? 
                        new MostOrderedProductResponse(null, "Ürün bulunamadı", 0, BigDecimal.ZERO) :
                        results.get(0);
            }
        }
        
        // dealerId parametresi verilmişse
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            // SUPER_ADMIN belirli dealer'ın en çok sipariş alan ürününü görebilir
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new EntityNotFoundException("Dealer bulunamadı"));
            List<MostOrderedProductResponse> results = productRepository.findMostOrderedProductByDealer(dealer);
            return results.isEmpty() ? 
                    new MostOrderedProductResponse(null, "Ürün bulunamadı", 0, BigDecimal.ZERO) :
                    results.get(0);
        } else {
            // Diğer roller sadece kendi dealer'ının en çok sipariş alan ürününü görebilir
            if (currentUser.getDealer() == null) {
                throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
            }
            
            // Verilen dealerId kullanıcının kendi dealer'ı değilse hata ver
            if (!dealerId.equals(currentUser.getDealer().getId())) {
                throw new IllegalStateException("Sadece kendi dealer'ınızın istatistiklerini görüntüleyebilirsiniz");
            }
            
            List<MostOrderedProductResponse> results = productRepository.findMostOrderedProductByDealer(currentUser.getDealer());
            return results.isEmpty() ? 
                    new MostOrderedProductResponse(null, "Ürün bulunamadı", 0, BigDecimal.ZERO) :
                    results.get(0);
        }
    }

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }
} 