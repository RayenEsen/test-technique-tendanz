package com.tendanz.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity tracking the modification history of a quote.
 * Each record represents one change event on a quote.
 */
@Entity
@Table(name = "quote_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // CREATED, UPDATED

    @Column(name = "previous_final_price")
    private BigDecimal previousFinalPrice;

    @Column(name = "new_final_price")
    private BigDecimal newFinalPrice;

    @Column(name = "previous_zone_code", length = 50)
    private String previousZoneCode;

    @Column(name = "new_zone_code", length = 50)
    private String newZoneCode;

    @Column(name = "previous_product_name", length = 100)
    private String previousProductName;

    @Column(name = "new_product_name", length = 100)
    private String newProductName;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
