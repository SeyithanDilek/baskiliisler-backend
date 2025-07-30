package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrue();
    
    List<Product> findByActiveFalse();
    
    List<Product> findByDealer(Dealer dealer);
}
