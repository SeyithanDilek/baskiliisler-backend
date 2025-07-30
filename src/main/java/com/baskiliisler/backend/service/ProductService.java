package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
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
    public List<Product> getAllProducts() {
        User currentUser = getCurrentUser();
        
        // SUPER_ADMIN tüm ürünleri görebilir
        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return productRepository.findAll();
        }
        
        // Diğer roller sadece kendi dealer'ının ürünlerini görebilir
        if (currentUser.getDealer() == null) {
            throw new IllegalStateException("Kullanıcının atanmış bir dealer'ı yok");
        }
        
        return productRepository.findByDealer(currentUser.getDealer());
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

    private User getCurrentUser() {
        return userRepository.findById(SecurityUtil.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Kullanıcı bulunamadı"));
    }
} 