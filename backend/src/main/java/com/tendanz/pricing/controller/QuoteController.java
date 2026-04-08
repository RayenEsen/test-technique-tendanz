package com.tendanz.pricing.controller;

import com.tendanz.pricing.dto.PagedResponse;
import com.tendanz.pricing.dto.QuoteHistoryResponse;
import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.service.PdfExportService;
import com.tendanz.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing quotes.
 */
@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Slf4j
public class QuoteController {

    private final PricingService pricingService;
    private final PdfExportService pdfExportService;

    /**
     * Create a new quote.
     * POST /api/quotes
     */
    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("Creating quote for client: {}, product: {}, zone: {}", request.getClientName(), request.getProductId(), request.getZoneCode());
        QuoteResponse response = pricingService.calculateQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a quote by ID.
     * GET /api/quotes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long id) {
        log.info("Fetching quote with ID: {}", id);
        return ResponseEntity.ok(pricingService.getQuote(id));
    }

    /**
     * Get all quotes with optional filters.
     * GET /api/quotes?productId=1&minPrice=500
     * Supports pagination via ?page=0&size=10 (returns PagedResponse when used).
     */
    @GetMapping
    public ResponseEntity<?> getAllQuotes(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        log.info("Fetching quotes - productId: {}, minPrice: {}, page: {}, size: {}", productId, minPrice, page, size);

        if (page != null && size != null) {
            PagedResponse<QuoteResponse> paged = pricingService.getQuotesPaged(productId, minPrice, page, size);
            return ResponseEntity.ok(paged);
        }

        List<QuoteResponse> quotes = pricingService.getQuotes(productId, minPrice);
        return ResponseEntity.ok(quotes);
    }

    /**
     * Export a quote as a PDF file.
     * GET /api/quotes/{id}/pdf
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportQuotePdf(@PathVariable Long id) {
        log.info("Exporting PDF for quote ID: {}", id);
        QuoteResponse quote = pricingService.getQuote(id);
        byte[] pdf = pdfExportService.generateQuotePdf(quote);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quote-" + id + ".pdf");
        headers.setContentLength(pdf.length);

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    /**
     * Get the modification history of a quote.
     * GET /api/quotes/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<QuoteHistoryResponse>> getQuoteHistory(@PathVariable Long id) {
        log.info("Fetching history for quote ID: {}", id);
        return ResponseEntity.ok(pricingService.getQuoteHistory(id));
    }
}
