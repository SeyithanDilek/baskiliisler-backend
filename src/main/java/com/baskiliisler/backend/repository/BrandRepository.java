package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.dto.MostQuotedBrandResponse;
import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    @Query("SELECT b FROM Brand b ORDER BY b.updatedAt DESC")
    List<Brand> findAll();
    
    Optional<Brand> findByName(String name);
    Optional<Brand> findByNameIgnoreCase(String name);
    List<Brand> findByDealer(Dealer dealer);

    @Query("SELECT new com.baskiliisler.backend.dto.MostQuotedBrandResponse(" +
           "b.id, b.name, CAST(COUNT(q) AS INTEGER), COALESCE(SUM(q.totalPrice), 0)) " +
           "FROM Brand b " +
           "LEFT JOIN Quote q ON q.brand = b " +
           "GROUP BY b.id, b.name " +
           "ORDER BY COUNT(q) DESC, COALESCE(SUM(q.totalPrice), 0) DESC")
    List<MostQuotedBrandResponse> findMostQuotedBrand();

    @Query("SELECT new com.baskiliisler.backend.dto.MostQuotedBrandResponse(" +
           "b.id, b.name, CAST(COUNT(q) AS INTEGER), COALESCE(SUM(q.totalPrice), 0)) " +
           "FROM Brand b " +
           "LEFT JOIN Quote q ON q.brand = b " +
           "WHERE b.dealer = :dealer " +
           "GROUP BY b.id, b.name " +
           "ORDER BY COUNT(q) DESC, COALESCE(SUM(q.totalPrice), 0) DESC")
    List<MostQuotedBrandResponse> findMostQuotedBrandByDealer(@Param("dealer") Dealer dealer);
    
    Integer countByDealer(Dealer dealer);
    
    // Authorization queries for dealer users
    List<Brand> findByAssignedUser(User user);
    
    @Query("SELECT b FROM Brand b WHERE b.assignedUser = :user ORDER BY b.updatedAt DESC")
    List<Brand> findByAssignedUserOrderByUpdatedAtDesc(@Param("user") User user);
    
    @Query("SELECT b FROM Brand b WHERE b.assignedUser = :user AND b.dealer = :dealer ORDER BY b.updatedAt DESC")
    List<Brand> findByAssignedUserAndDealer(@Param("user") User user, @Param("dealer") Dealer dealer);
}
