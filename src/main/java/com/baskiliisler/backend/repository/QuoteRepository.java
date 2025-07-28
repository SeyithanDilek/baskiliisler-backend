package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Brand;
import com.baskiliisler.backend.model.Quote;
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
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand")
    List<Quote> findAllWithBrand();
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand LEFT JOIN FETCH q.items WHERE q.id = :id")
    Optional<Quote> findByIdWithBrandAndItems(@Param("id") Long id);
    
    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.brand WHERE q.brand = :brand")
    List<Quote> findByBrandWithBrand(@Param("brand") Brand brand);
}
