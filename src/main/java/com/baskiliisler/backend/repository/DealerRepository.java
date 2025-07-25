package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    
    Optional<Dealer> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT d FROM Dealer d WHERE d.master = true")
    Optional<Dealer> findMasterDealer();
} 