package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Dealer;
import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.model.User;
import com.baskiliisler.backend.type.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByStatusAndValidUntilBefore(QuoteStatus status, LocalDate date);
    List<Quote> findByBrand(Brand brand);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.items WHERE q.id = :id")
    Optional<Quote> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand ORDER BY q.updatedAt DESC")
    List<Quote> findAllWithBrand();
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand LEFT JOIN FETCH q.items WHERE q.id = :id")
    Optional<Quote> findByIdWithBrandAndItems(@Param("id") Long id);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand WHERE q.brand = :brand ORDER BY q.updatedAt DESC")
    List<Quote> findByBrandWithBrand(@Param("brand") Brand brand);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand WHERE q.brand.dealer = :dealer ORDER BY q.updatedAt DESC")
    List<Quote> findByDealerWithBrand(@Param("dealer") Dealer dealer);
    
    Integer countByDealer(Dealer dealer);
    
    // Yeni istatistik method'ları
    @Query("SELECT q FROM Quote q WHERE q.brand.dealer = :dealer AND q.validUntil BETWEEN :startDate AND :endDate AND q.status = :status")
    List<Quote> findByDealerAndValidUntilBetweenAndStatus(@Param("dealer") Dealer dealer, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate, 
                                                         @Param("status") QuoteStatus status);
    
    @Query("SELECT q FROM Quote q WHERE q.brand.dealer = :dealer AND q.validUntil <= :expiryDate AND q.status = :status")
    List<Quote> findByDealerAndValidUntilBeforeAndStatus(@Param("dealer") Dealer dealer, 
                                                        @Param("expiryDate") LocalDate expiryDate, 
                                                        @Param("status") QuoteStatus status);
    
    @Query("SELECT COUNT(q), COALESCE(SUM(q.totalPrice), 0) FROM Quote q WHERE q.brand.dealer = :dealer AND q.status = :status")
    Object[] countAndSumByDealerAndStatus(@Param("dealer") Dealer dealer, @Param("status") QuoteStatus status);
    
    @Query("SELECT COUNT(q), COALESCE(SUM(q.totalPrice), 0) FROM Quote q WHERE q.brand.dealer = :dealer")
    Object[] countAndSumByDealer(@Param("dealer") Dealer dealer);
    
    // Authorization queries for dealer users
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand WHERE q.brand.assignedUser = :user ORDER BY q.updatedAt DESC")
    List<Quote> findByBrandAssignedUser(@Param("user") User user);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand LEFT JOIN FETCH q.items WHERE q.brand.assignedUser = :user ORDER BY q.updatedAt DESC")
    List<Quote> findByBrandAssignedUserWithItems(@Param("user") User user);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand WHERE q.brand.assignedUser = :user AND q.status = :status ORDER BY q.updatedAt DESC")
    List<Quote> findByBrandAssignedUserAndStatus(@Param("user") User user, @Param("status") QuoteStatus status);
    
    @Query("SELECT COUNT(q), COALESCE(SUM(q.totalPrice), 0) FROM Quote q WHERE q.brand.assignedUser = :user")
    Object[] countAndSumByBrandAssignedUser(@Param("user") User user);
    
    @Query("SELECT COUNT(q), COALESCE(SUM(q.totalPrice), 0) FROM Quote q WHERE q.brand.assignedUser = :user AND q.status = :status")
    Object[] countAndSumByBrandAssignedUserAndStatus(@Param("user") User user, @Param("status") QuoteStatus status);
    
    // Teklif hatırlatma için - belirli tarihte süresi dolacak teklifleri bul
    @Query("SELECT q FROM Quote q WHERE q.status = :status AND q.validUntil = :validUntil")
    List<Quote> findByStatusAndValidUntil(@Param("status") QuoteStatus status, @Param("validUntil") LocalDate validUntil);
}
