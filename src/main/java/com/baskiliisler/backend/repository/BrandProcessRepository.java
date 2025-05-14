package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.BrandProcess;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandProcessRepository extends JpaRepository<BrandProcess, Long> {
    Optional<BrandProcess> findByBrandId(Long brandId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BrandProcess p join fetch p.brand where p.brand.id = :brandId")
    Optional<BrandProcess> findByBrandIdForUpdate(@Param("brandId") Long brandId);

    boolean existsByBrandId(Long brandId);
}
