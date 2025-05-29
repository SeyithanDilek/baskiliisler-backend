package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    boolean existsByCode(String code);
    
    Optional<Product> findByCode(String code);
    
    List<Product> findByActiveTrue();
    
    List<Product> findByActiveFalse();
}
