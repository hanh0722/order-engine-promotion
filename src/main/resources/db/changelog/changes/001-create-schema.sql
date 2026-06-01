-- liquibase formatted sql

-- changeset engine:1
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    discount_amount NUMERIC(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    code VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);

-- changeset engine:2
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- changeset engine:3
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    value NUMERIC(19,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- changeset engine:4
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_type VARCHAR(50) NOT NULL,
    sub_total NUMERIC(19,2) NOT NULL,
    total_discount NUMERIC(19,2) NOT NULL,
    final_price NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- changeset engine:5
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku VARCHAR(255) NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);

-- changeset engine:6
-- INDEXES FOR coupons

CREATE INDEX idx_coupons_active ON coupons(active);

CREATE UNIQUE INDEX uk_coupons_code ON coupons(code);

-- changeset engine:7
-- INDEXES FOR products

CREATE INDEX idx_products_sku ON products(sku);

-- changeset engine:8
-- INDEXES FOR promotions

CREATE INDEX idx_promotions_active ON promotions(active);

-- changeset engine:9
-- INDEXES FOR order_items

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

CREATE INDEX idx_order_items_sku ON order_items(sku);