package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.ProcessHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessHistoryRepository extends JpaRepository<ProcessHistory, Long> {

    @Query("select h from ProcessHistory h where h.process.brand.id = :brandId order by h.changedAt desc")
    List<ProcessHistory> findByBrandId(Long brandId);
}
