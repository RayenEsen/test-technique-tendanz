package com.tendanz.pricing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tendanz.pricing.dto.PagedResponse;
import com.tendanz.pricing.dto.QuoteHistoryResponse;
import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.entity.PricingRule;
import com.tendanz.pricing.entity.Product;
import com.tendanz.pricing.entity.Quote;
import com.tendanz.pricing.entity.QuoteHistory;
import com.tendanz.pricing.entity.Zone;
import com.tendanz.pricing.enums.AgeCategory;
import com.tendanz.pricing.repository.PricingRuleRepository;
import com.tendanz.pricing.repository.ProductRepository;
import com.tendanz.pricing.repository.QuoteHistoryRepository;
import com.tendanz.pricing.repository.QuoteRepository;
import com.tendanz.pricing.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling pricing and quote calculations.
 * Manages the business logic for pricing rules and quote generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteHistoryRepository quoteHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Calculate and persist a new quote based on the provided request.
     *
     * @param request the quote request containing productId, zoneCode, clientName, clientAge
     * @return the calculated quote response
     * @throws IllegalArgumentException if product, zone, or pricing rule not found
     */
    @Transactional
    public QuoteResponse calculateQuote(QuoteRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + request.getProductId()));

        Zone zone = zoneRepository.findByCode(request.getZoneCode())
                .orElseThrow(() -> new IllegalArgumentException("Zone not found with code: " + request.getZoneCode()));

        PricingRule pricingRule = pricingRuleRepository.findByProductId(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("No pricing rule found for product: " + product.getName()));

        AgeCategory ageCategory = AgeCategory.fromAge(request.getClientAge());
        BigDecimal ageFactor = getAgeFactor(pricingRule, ageCategory);
        BigDecimal baseRate = pricingRule.getBaseRate();

        BigDecimal finalPrice = baseRate
                .multiply(ageFactor)
                .multiply(zone.getRiskCoefficient())
                .setScale(2, RoundingMode.HALF_UP);

        List<String> appliedRules = buildAppliedRules(product, zone, ageCategory, ageFactor, baseRate, finalPrice, request.getClientAge());

        Quote quote = Quote.builder()
                .product(product)
                .zone(zone)
                .clientName(request.getClientName())
                .clientAge(request.getClientAge())
                .basePrice(baseRate)
                .finalPrice(finalPrice)
                .appliedRules(convertRulesToJson(appliedRules))
                .build();

        quoteRepository.save(quote);

        // Record creation in history
        quoteHistoryRepository.save(QuoteHistory.builder()
                .quote(quote)
                .changeType("CREATED")
                .newFinalPrice(finalPrice)
                .newZoneCode(zone.getCode())
                .newProductName(product.getName())
                .changeDescription("Quote created for client " + request.getClientName())
                .build());

        log.info("Quote created: ID={}, client={}, finalPrice={}", quote.getId(), quote.getClientName(), finalPrice);
        return mapToResponse(quote, appliedRules);
    }

    /**
     * Get a single quote by ID.
     *
     * @param id the quote ID
     * @return the quote response
     * @throws IllegalArgumentException if quote not found
     */
    public QuoteResponse getQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + id));
        return mapToResponse(quote, deserializeRules(quote.getAppliedRules()));
    }

    /**
     * Get all quotes with optional filters (non-paginated).
     *
     * @param productId optional product filter
     * @param minPrice  optional minimum price filter
     * @return list of matching quotes
     */
    public List<QuoteResponse> getQuotes(Long productId, Double minPrice) {
        List<Quote> quotes;
        if (productId != null) {
            quotes = quoteRepository.findByProductId(productId);
        } else if (minPrice != null) {
            quotes = quoteRepository.findByFinalPriceGreaterThanEqual(BigDecimal.valueOf(minPrice));
        } else {
            quotes = quoteRepository.findAll();
        }
        return quotes.stream()
                .map(q -> mapToResponse(q, deserializeRules(q.getAppliedRules())))
                .toList();
    }

    /**
     * Get paginated quotes with optional filters.
     *
     * @param productId optional product filter
     * @param minPrice  optional minimum price filter
     * @param page      zero-based page index
     * @param size      page size
     * @return paginated response
     */
    public PagedResponse<QuoteResponse> getQuotesPaged(Long productId, Double minPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Quote> quotePage;

        if (productId != null) {
            quotePage = quoteRepository.findByProductId(productId, pageable);
        } else if (minPrice != null) {
            quotePage = quoteRepository.findByFinalPriceGreaterThanEqual(BigDecimal.valueOf(minPrice), pageable);
        } else {
            quotePage = quoteRepository.findAll(pageable);
        }

        List<QuoteResponse> content = quotePage.getContent().stream()
                .map(q -> mapToResponse(q, deserializeRules(q.getAppliedRules())))
                .toList();

        return PagedResponse.<QuoteResponse>builder()
                .content(content)
                .page(quotePage.getNumber())
                .size(quotePage.getSize())
                .totalElements(quotePage.getTotalElements())
                .totalPages(quotePage.getTotalPages())
                .last(quotePage.isLast())
                .build();
    }

    /**
     * Get the modification history of a quote.
     *
     * @param quoteId the quote ID
     * @return list of history entries ordered by most recent first
     */
    public List<QuoteHistoryResponse> getQuoteHistory(Long quoteId) {
        if (!quoteRepository.existsById(quoteId)) {
            throw new IllegalArgumentException("Quote not found with ID: " + quoteId);
        }
        return quoteHistoryRepository.findByQuoteIdOrderByChangedAtDesc(quoteId).stream()
                .map(this::mapHistoryToResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<String> buildAppliedRules(Product product, Zone zone, AgeCategory ageCategory,
                                            BigDecimal ageFactor, BigDecimal baseRate,
                                            BigDecimal finalPrice, int clientAge) {
        List<String> rules = new ArrayList<>();
        rules.add("Product: " + product.getName() + " - Base rate: " + baseRate + " TND");
        rules.add("Age category: " + ageCategory.name() + " (age " + clientAge + ") - Age factor: " + ageFactor);
        rules.add("Zone: " + zone.getName() + " (" + zone.getCode() + ") - Zone coefficient: " + zone.getRiskCoefficient());
        rules.add("Final price calculation: " + baseRate + " x " + ageFactor + " x " + zone.getRiskCoefficient() + " = " + finalPrice + " TND");
        return rules;
    }

    private BigDecimal getAgeFactor(PricingRule pricingRule, AgeCategory ageCategory) {
        return switch (ageCategory) {
            case YOUNG -> pricingRule.getAgeFactorYoung();
            case ADULT -> pricingRule.getAgeFactorAdult();
            case SENIOR -> pricingRule.getAgeFactorSenior();
            case ELDERLY -> pricingRule.getAgeFactorElderly();
        };
    }

    private String convertRulesToJson(List<String> rules) {
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (Exception e) {
            log.error("Error converting rules to JSON", e);
            return "[]";
        }
    }

    private List<String> deserializeRules(String rulesJson) {
        try {
            return objectMapper.readValue(rulesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.error("Error deserializing rules from JSON", e);
            return new ArrayList<>();
        }
    }

    private QuoteResponse mapToResponse(Quote quote, List<String> appliedRules) {
        return QuoteResponse.builder()
                .quoteId(quote.getId())
                .productName(quote.getProduct().getName())
                .zoneName(quote.getZone().getName())
                .clientName(quote.getClientName())
                .clientAge(quote.getClientAge())
                .basePrice(quote.getBasePrice())
                .finalPrice(quote.getFinalPrice())
                .appliedRules(appliedRules)
                .createdAt(quote.getCreatedAt())
                .build();
    }

    private QuoteHistoryResponse mapHistoryToResponse(QuoteHistory h) {
        return QuoteHistoryResponse.builder()
                .id(h.getId())
                .quoteId(h.getQuote().getId())
                .changedAt(h.getChangedAt())
                .changeType(h.getChangeType())
                .previousFinalPrice(h.getPreviousFinalPrice())
                .newFinalPrice(h.getNewFinalPrice())
                .previousZoneCode(h.getPreviousZoneCode())
                .newZoneCode(h.getNewZoneCode())
                .previousProductName(h.getPreviousProductName())
                .newProductName(h.getNewProductName())
                .changeDescription(h.getChangeDescription())
                .build();
    }
}
