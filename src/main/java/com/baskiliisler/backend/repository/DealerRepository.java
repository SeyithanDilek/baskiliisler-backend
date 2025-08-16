package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    @Query("SELECT d FROM Dealer d ORDER BY d.updatedAt DESC")
    List<Dealer> findAll();
    
    List<Dealer> findByActive(boolean active);
    Optional<Dealer> findByName(String name);
    boolean existsByName(String name);
} 