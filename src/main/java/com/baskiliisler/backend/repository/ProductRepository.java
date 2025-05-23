package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
