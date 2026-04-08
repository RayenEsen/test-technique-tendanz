package com.tendanz.pricing.controller;

import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing quotes.
 * Handles all quote-related API endpoints.
 *
 * TODO: Implement the following endpoints:
 * - POST /api/quotes       -> Create a new quote (call PricingService.calculateQuote)
 * - GET  /api/quotes/{id}  -> Already implemented below as reference
 * - GET  /api/quotes       -> Get all quotes with optional filters (productId, minPrice)
 */
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Slf4j
public class QuoteController {

    private final PricingService pricingService;

    /**
     * TODO: Create a new quote.
     *
     * Requirements:
     * - Accept a QuoteRequest body with @Valid validation
     * - Call PricingService.calculateQuote()
     * - Return HTTP 201 CREATED with the QuoteResponse
     * - Let the GlobalExceptionHandler handle errors
     *
     * @param request the quote request
     * @return the created quote response with 201 status
     */
    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("Creating quote for client: {}, product: {}, zone: {}", request.getClientName(), request.getProductId(), request.getZoneCode());
        QuoteResponse response = pricingService.calculateQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a quote by ID.
     * This endpoint is provided as a reference implementation.
     *
     * @param id the quote ID
     * @return the quote response
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long id) {
        log.info("Fetching quote with ID: {}", id);
        QuoteResponse response = pricingService.getQuote(id);
        return ResponseEntity.ok(response);
    }

    /**
     * TODO: Get all quotes with optional filters.
     *
     * Requirements:
     * - Support optional query parameter: productId (Long) to filter by product
     * - Support optional query parameter: minPrice (Double) to filter by minimum final price
     * - Use QuoteRepository methods for querying
     * - Convert Quote entities to QuoteResponse DTOs
     * - Return HTTP 200 OK with the list
     *
     * Examples:
     * - GET /api/quotes                          -> all quotes
     * - GET /api/quotes?productId=1              -> quotes for product 1
     * - GET /api/quotes?minPrice=500             -> quotes with finalPrice >= 500
     * - GET /api/quotes?productId=1&minPrice=500 -> combined filters
     *
     * @param productId optional product ID filter
     * @param minPrice optional minimum price filter
     * @return list of quotes matching filters
     */
    @GetMapping
    public ResponseEntity<List<QuoteResponse>> getAllQuotes(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Double minPrice) {
        log.info("Fetching quotes with filters - productId: {}, minPrice: {}", productId, minPrice);
        List<QuoteResponse> responses = pricingService.getQuotes(productId, minPrice);
        return ResponseEntity.ok(responses);
    }
}
