package com.engine.order_engine.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.dto.OrderItemRequest;
import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.domain.dto.promotion.Promotion;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.PromotionType;
import com.engine.order_engine.domain.promotion.chain.PromotionHandler;
import com.engine.order_engine.domain.promotion.chain.PromotionStrategyHandler;
import com.engine.order_engine.domain.promotion.strategy.Buy2Get1Strategy;
import com.engine.order_engine.domain.promotion.strategy.CouponStrategy;
import com.engine.order_engine.domain.promotion.strategy.PercentageDiscountStrategy;
import com.engine.order_engine.domain.promotion.strategy.VipCustomerStrategy;
import com.engine.order_engine.entity.CouponEntity;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.mapper.coupon.CouponMapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PromotionTestFixtures {

    private PromotionTestFixtures() {
    }

    public static OrderItemRequest item(String sku, String price, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setSku(sku);
        item.setPrice(new BigDecimal(price));
        item.setQuantity(quantity);
        return item;
    }

    public static Promotion promotion(PromotionType type, String value) {
        Promotion promotion = Promotion.builder().type(type).value(new BigDecimal(value)).active(true).build();
        return promotion;
    }

    public static PromotionContext context(
            CustomerType customerType,
            Coupon coupon,
            List<OrderItemRequest> items,
            List<Promotion> activePromotions) {
        return new PromotionContext(customerType, coupon, items, activePromotions);
    }

    public static PromotionContext assignmentExampleContext(Coupon coupon) {
        return context(
                CustomerType.VIP,
                coupon,
                List.of(
                        item("A100", "100", 2),
                        item("B200", "50", 1)),
                List.of(
                        promotion(PromotionType.PERCENTAGE_DISCOUNT, "10"),
                        promotion(PromotionType.VIP_DISCOUNT, "5"),
                        promotion(PromotionType.BUY2_GET1_FREE, "0")));
    }

    public static Coupon summer10Coupon() {
        Coupon coupon = mock(Coupon.class);
        when(coupon.getCode()).thenReturn("SUMMER10");
        when(coupon.getActive()).thenReturn(true);
        when(coupon.getDiscountAmount()).thenReturn(new BigDecimal("10.00"));
        when(coupon.getExpiryDate()).thenReturn(Instant.parse("2099-12-31T23:59:59Z"));
        when(coupon.getQuantity()).thenReturn(2);
        return coupon;
    }

    public static PromotionPipeline productionPipeline() {
        PromotionHandler head = new PromotionStrategyHandler(new PercentageDiscountStrategy());
        head.link(new PromotionStrategyHandler(new Buy2Get1Strategy()))
                .link(new PromotionStrategyHandler(new CouponStrategy()))
                .link(new PromotionStrategyHandler(new VipCustomerStrategy()));
        return new PromotionPipeline(head);
    }
}
