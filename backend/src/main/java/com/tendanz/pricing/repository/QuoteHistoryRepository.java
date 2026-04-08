package com.tendanz.pricing.repository;

import com.tendanz.pricing.entity.QuoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for QuoteHistory entity.
 */
@Repository
public interface QuoteHistoryRepository extends JpaRepository<QuoteHistory, Long> {

    List<QuoteHistory> findByQuoteIdOrderByChangedAtDesc(Long quoteId);
}
