-- Zone Table
CREATE TABLE zone (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    risk_coefficient DECIMAL(5, 2) NOT NULL
);

-- Product Table
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Pricing Rule Table
CREATE TABLE pricing_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    base_rate DECIMAL(10, 2) NOT NULL,
    age_factor_young DECIMAL(5, 2) NOT NULL,
    age_factor_adult DECIMAL(5, 2) NOT NULL,
    age_factor_senior DECIMAL(5, 2) NOT NULL,
    age_factor_elderly DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

-- Quote Table
CREATE TABLE quote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    zone_id BIGINT NOT NULL,
    client_name VARCHAR(100) NOT NULL,
    client_age INT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    applied_rules CLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (zone_id) REFERENCES zone(id)
);

-- Create Indexes
CREATE INDEX idx_quote_product_id ON quote(product_id);
CREATE INDEX idx_quote_zone_id ON quote(zone_id);
CREATE INDEX idx_quote_client_name ON quote(client_name);
CREATE INDEX idx_quote_created_at ON quote(created_at);
CREATE INDEX idx_pricing_rule_product_id ON pricing_rule(product_id);

-- Quote History Table (bonus: tracks modifications)
CREATE TABLE quote_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_type VARCHAR(20) NOT NULL,
    previous_final_price DECIMAL(10, 2),
    new_final_price DECIMAL(10, 2),
    previous_zone_code VARCHAR(50),
    new_zone_code VARCHAR(50),
    previous_product_name VARCHAR(100),
    new_product_name VARCHAR(100),
    change_description VARCHAR(500),
    FOREIGN KEY (quote_id) REFERENCES quote(id)
);
