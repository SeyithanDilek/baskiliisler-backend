package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
    @Query("SELECT f FROM Factory f ORDER BY f.updatedAt DESC")
    List<Factory> findAll();
    
    List<Factory> findByActiveTrue();
    
    java.util.Optional<Factory> findByName(String name);
}
