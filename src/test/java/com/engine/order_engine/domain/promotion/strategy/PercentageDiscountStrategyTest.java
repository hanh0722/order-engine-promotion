package com.engine.order_engine.domain.promotion.strategy;

import static com.engine.order_engine.support.PromotionTestFixtures.context;
import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static com.engine.order_engine.support.PromotionTestFixtures.promotion;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.promotion.PromotionType;

class PercentageDiscountStrategyTest {

    private PercentageDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
    }

    @Test
    void apply_appliesTenPercentOffSubtotal() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("A100", "100", 2)),
                List.of(promotion(PromotionType.PERCENTAGE_DISCOUNT, "10")));

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountType()).isEqualTo("PERCENTAGE_DISCOUNT");
        assertThat(result.get().getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void apply_returnsEmptyWhenNoPercentagePromotionActive() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("A100", "100", 1)),
                List.of(promotion(PromotionType.BUY2_GET1_FREE, "0")));

        assertThat(strategy.apply(promotionContext)).isEmpty();
    }

    @Test
    void getPromotionType_isPercentageDiscount() {
        assertThat(strategy.getPromotionType()).isEqualTo(PromotionType.PERCENTAGE_DISCOUNT);
    }
}
