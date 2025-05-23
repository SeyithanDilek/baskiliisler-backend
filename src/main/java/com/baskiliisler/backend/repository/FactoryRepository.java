package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
    List<Factory> findByActiveTrue();
}
