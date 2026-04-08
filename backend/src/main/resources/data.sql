-- ============================================================
-- Initial Data for Insurance Pricing Engine
-- ============================================================

-- Zones (code, name, risk_coefficient)
INSERT INTO zone (code, name, risk_coefficient) VALUES
('TUN', 'Grand Tunis', 1.20),
('SFX', 'Sfax', 1.00),
('SOU', 'Sousse', 1.10);

INSERT INTO product (name, description, created_at) VALUES
('Assurance Auto', 'Comprehensive car insurance coverage', CURRENT_TIMESTAMP),
('Assurance Habitation', 'Home and property protection insurance', CURRENT_TIMESTAMP),
('Assurance Santé', 'Health insurance and medical expense coverage', CURRENT_TIMESTAMP);

-- Pricing Rules (product_id, base_rate, age factors, created_at)
-- Age factors: YOUNG (18-24): 1.30 | ADULT (25-45): 1.00 | SENIOR (46-65): 1.20 | ELDERLY (66-99): 1.50

-- Assurance Auto: base_rate = 500.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (1, 500.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);

-- Assurance Habitation: base_rate = 300.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (2, 300.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);

-- Assurance Santé: base_rate = 800.00 TND
INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (3, 800.00, 1.30, 1.00, 1.20, 1.50, CURRENT_TIMESTAMP);

-- 4th product: Assurance Voyage (Travel Insurance)
-- Higher risk for young and elderly travelers, base_rate = 250.00 TND
INSERT INTO product (name, description, created_at) VALUES
('Assurance Voyage', 'Travel insurance and international assistance coverage', CURRENT_TIMESTAMP);

INSERT INTO pricing_rule (product_id, base_rate, age_factor_young, age_factor_adult, age_factor_senior, age_factor_elderly, created_at)
VALUES (4, 250.00, 1.40, 1.00, 1.30, 1.70, CURRENT_TIMESTAMP);
