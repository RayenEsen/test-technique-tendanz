package com.tendanz.pricing;

import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.entity.PricingRule;
import com.tendanz.pricing.entity.Product;
import com.tendanz.pricing.entity.Quote;
import com.tendanz.pricing.entity.Zone;
import com.tendanz.pricing.repository.PricingRuleRepository;
import com.tendanz.pricing.repository.ProductRepository;
import com.tendanz.pricing.repository.QuoteRepository;
import com.tendanz.pricing.repository.ZoneRepository;
import com.tendanz.pricing.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PricingService.
 *
 * TODO: Implement at least 5 test cases covering:
 * - Quote calculation for different age categories (YOUNG, ADULT, SENIOR, ELDERLY)
 * - Different zone risk coefficients
 * - Edge cases (minimum age 18, maximum age 99, boundary between categories)
 * - Error handling (invalid product ID, invalid zone code)
 * - Quote retrieval by ID
 *
 * The @BeforeEach setUp() method below creates test data you can use.
 * Add your test methods below the existing structure.
 */
@DataJpaTest
@Import({PricingService.class, ObjectMapper.class})
class PricingServiceTest {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    private Product product;
    private Zone zone;
    private PricingRule pricingRule;

    @BeforeEach
    void setUp() {
        // Test data: Auto Insurance, zone coefficient 1.20, standard age factors
        product = Product.builder()
                .name("Test Auto Insurance")
                .description("Test Description")
                .createdAt(LocalDateTime.now())
                .build();
        productRepository.save(product);

        // Reuse the zone already loaded by data.sql (TUN is already there)
        zone = zoneRepository.findByCode("TUN")
                .orElseGet(() -> {
                    Zone z = Zone.builder()
                            .code("TUN")
                            .name("Grand Tunis")
                            .riskCoefficient(BigDecimal.valueOf(1.20))
                            .build();
                    return zoneRepository.save(z);
                });

        pricingRule = PricingRule.builder()
                .product(product)
                .baseRate(BigDecimal.valueOf(500.00))
                .ageFactorYoung(BigDecimal.valueOf(1.30))
                .ageFactorAdult(BigDecimal.valueOf(1.00))
                .ageFactorSenior(BigDecimal.valueOf(1.20))
                .ageFactorElderly(BigDecimal.valueOf(1.50))
                .createdAt(LocalDateTime.now())
                .build();
        pricingRuleRepository.save(pricingRule);
    }

    @Test
    void testCalculateQuoteForAdult() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Alice Martin")
                .clientAge(30)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(0, new BigDecimal("500.00").compareTo(response.getBasePrice()));
        assertEquals(new BigDecimal("600.00"), response.getFinalPrice()); // 500 × 1.00 × 1.20
        assertEquals("Alice Martin", response.getClientName());
    }

    @Test
    void testCalculateQuoteForYoungClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Bob Young")
                .clientAge(22)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("780.00"), response.getFinalPrice()); // 500 × 1.30 × 1.20
    }

    @Test
    void testCalculateQuoteForSeniorClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Carol Senior")
                .clientAge(55)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("720.00"), response.getFinalPrice()); // 500 × 1.20 × 1.20
    }

    @Test
    void testCalculateQuoteWithInvalidProductId() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(9999L)
                .zoneCode("TUN")
                .clientName("Test Client")
                .clientAge(30)
                .build();

        assertThrows(IllegalArgumentException.class, () -> pricingService.calculateQuote(request));
    }

    @Test
    void testCalculateQuoteWithInvalidZoneCode() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("INVALID")
                .clientName("Test Client")
                .clientAge(30)
                .build();

        assertThrows(IllegalArgumentException.class, () -> pricingService.calculateQuote(request));
    }

    @Test
    void testCalculateQuoteForElderlyClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("David Elderly")
                .clientAge(70)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("900.00"), response.getFinalPrice()); // 500 × 1.50 × 1.20
    }

    @Test
    void testGetQuoteById() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Eve Retrieve")
                .clientAge(40)
                .build();

        QuoteResponse created = pricingService.calculateQuote(request);
        QuoteResponse fetched = pricingService.getQuote(created.getQuoteId());

        assertNotNull(fetched);
        assertEquals(created.getQuoteId(), fetched.getQuoteId());
        assertEquals(created.getFinalPrice(), fetched.getFinalPrice());
        assertEquals("Eve Retrieve", fetched.getClientName());
    }

    @Test
    void testAgeBoundaries() {
        // Age 24 → YOUNG, age 25 → ADULT
        QuoteRequest youngBoundary = QuoteRequest.builder()
                .productId(product.getId()).zoneCode("TUN").clientName("Young Boundary").clientAge(24).build();
        QuoteRequest adultBoundary = QuoteRequest.builder()
                .productId(product.getId()).zoneCode("TUN").clientName("Adult Boundary").clientAge(25).build();

        QuoteResponse youngResp = pricingService.calculateQuote(youngBoundary);
        QuoteResponse adultResp = pricingService.calculateQuote(adultBoundary);

        assertEquals(new BigDecimal("780.00"), youngResp.getFinalPrice()); // YOUNG: 500 × 1.30 × 1.20
        assertEquals(new BigDecimal("600.00"), adultResp.getFinalPrice()); // ADULT: 500 × 1.00 × 1.20
    }
}
