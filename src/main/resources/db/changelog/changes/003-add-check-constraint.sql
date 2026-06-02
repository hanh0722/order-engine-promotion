--liquibase formatted sql

--changeset table:add-check-constraints

ALTER TABLE coupons
ADD CONSTRAINT chk_coupon_quantity
CHECK (quantity >= 0);

ALTER TABLE coupons
ADD CONSTRAINT chk_coupon_discount
CHECK (discount_amount >= 0);

ALTER TABLE promotions
ADD CONSTRAINT chk_promotion_value
CHECK (value >= 0);

ALTER TABLE order_items
ADD CONSTRAINT chk_order_item_quantity
CHECK (quantity > 0);

ALTER TABLE orders
ADD CONSTRAINT chk_order_amounts
CHECK (
    sub_total >= 0
    AND total_discount >= 0
    AND final_price >= 0
);