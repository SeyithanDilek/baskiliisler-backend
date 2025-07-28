package com.baskiliisler.backend.repository;

import com.baskiliisler.backend.model.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {
}
