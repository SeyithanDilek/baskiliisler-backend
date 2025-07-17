package com.baskiliisler.backend.service;

import com.baskiliisler.backend.dto.ProductRequestDto;
import com.baskiliisler.backend.dto.ProductResponseDto;
import com.baskiliisler.backend.dto.ProductUpdateDto;
import com.baskiliisler.backend.mapper.ProductMapper;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(ProductRequestDto dto) {
        Product product = ProductMapper.toEntity(dto);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
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
} 