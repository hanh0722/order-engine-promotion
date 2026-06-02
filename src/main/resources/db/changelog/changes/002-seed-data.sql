-- liquibase formatted sql

-- changeset seed:1
INSERT INTO products (sku, name, price) VALUES
    ('A100', 'Product A', 100.00),
    ('B200', 'Product B', 50.00);

-- changeset seed:2
INSERT INTO promotions (type, value, active) VALUES
    ('PERCENTAGE_DISCOUNT', 10, TRUE),
    ('VIP_DISCOUNT', 5, TRUE),
    ('BUY2_GET1_FREE', 0, TRUE);

-- changeset seed:3
INSERT INTO coupons (code, discount_amount, active, expiry_date, quantity) VALUES
    ('SUMMER10', 10.00, TRUE, '2099-12-31', 10),
    ('SAVE20', 20.00, TRUE, '2099-12-31', 10);
