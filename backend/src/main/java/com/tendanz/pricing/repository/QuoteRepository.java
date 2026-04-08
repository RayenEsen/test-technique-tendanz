package com.tendanz.pricing.repository;

import com.tendanz.pricing.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Quote entity.
 * Provides database operations for quotes including pagination support.
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByClientName(String clientName);

    List<Quote> findByProductId(Long productId);

    Page<Quote> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT q FROM Quote q WHERE q.finalPrice >= :minPrice")
    List<Quote> findByFinalPriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice);

    @Query("SELECT q FROM Quote q WHERE q.finalPrice >= :minPrice")
    Page<Quote> findByFinalPriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice, Pageable pageable);
}
