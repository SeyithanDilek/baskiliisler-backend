package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.dto.MostOrderedProductResponse;
import com.baskiliisler.backend.model.Product;
import com.baskiliisler.backend.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p ORDER BY p.updatedAt DESC")
    List<Product> findAll();
    
    List<Product> findByActiveTrue();
    
    List<Product> findByActiveFalse();
    
    List<Product> findByDealer(Dealer dealer);
    
    @Query("SELECT new com.baskiliisler.backend.dto.MostOrderedProductResponse(" +
           "p.id, p.name, CAST(COUNT(oi) AS INTEGER), COALESCE(SUM(oi.lineTotalWithTax), 0)) " +
           "FROM Product p " +
           "LEFT JOIN OrderItem oi ON oi.product = p " +
           "GROUP BY p.id, p.name " +
           "ORDER BY COUNT(oi) DESC, COALESCE(SUM(oi.lineTotalWithTax), 0) DESC")
    List<MostOrderedProductResponse> findMostOrderedProduct();
    
    @Query("SELECT new com.baskiliisler.backend.dto.MostOrderedProductResponse(" +
           "p.id, p.name, CAST(COUNT(oi) AS INTEGER), COALESCE(SUM(oi.lineTotalWithTax), 0)) " +
           "FROM Product p " +
           "LEFT JOIN OrderItem oi ON oi.product = p " +
           "WHERE p.dealer = :dealer " +
           "GROUP BY p.id, p.name " +
           "ORDER BY COUNT(oi) DESC, COALESCE(SUM(oi.lineTotalWithTax), 0) DESC")
    List<MostOrderedProductResponse> findMostOrderedProductByDealer(@Param("dealer") Dealer dealer);
    
    Integer countByDealer(Dealer dealer);
    
    // Authorization queries for dealer users
    @Query("SELECT p FROM Product p WHERE p.dealer = :dealer AND p.active = true ORDER BY p.updatedAt DESC")
    List<Product> findByDealerAndActiveTrue(@Param("dealer") Dealer dealer);
    
    @Query("SELECT p FROM Product p WHERE p.dealer = :dealer ORDER BY p.updatedAt DESC")
    List<Product> findByDealerOrderByUpdatedAtDesc(@Param("dealer") Dealer dealer);
}
