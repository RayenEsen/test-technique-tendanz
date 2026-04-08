package com.tendanz.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a single history entry for a quote.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteHistoryResponse {

    private Long id;
    private Long quoteId;
    private LocalDateTime changedAt;
    private String changeType;
    private BigDecimal previousFinalPrice;
    private BigDecimal newFinalPrice;
    private String previousZoneCode;
    private String newZoneCode;
    private String previousProductName;
    private String newProductName;
    private String changeDescription;
}
