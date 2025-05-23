package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.type.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByStatusAndValidUntilBefore(QuoteStatus status, LocalDate date);
}
