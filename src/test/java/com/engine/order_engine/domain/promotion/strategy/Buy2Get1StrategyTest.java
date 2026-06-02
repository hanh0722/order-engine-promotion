package com.engine.order_engine.domain.promotion.strategy;

import static com.engine.order_engine.support.PromotionTestFixtures.context;
import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.promotion.PromotionType;

class Buy2Get1StrategyTest {

    private Buy2Get1Strategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new Buy2Get1Strategy();
    }

    @Test
    void apply_oneFreeUnitForEveryTwoUnitsOfSameSku() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("A100", "100", 2)),
                List.of());

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountType()).isEqualTo("BUY2_GET1_FREE");
        assertThat(result.get().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void apply_noFreeUnitsWhenQuantityIsOne() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("B200", "50", 1)),
                List.of());

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void apply_sumsFreeUnitsAcrossMultipleSkus() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(
                        item("A100", "100", 4),
                        item("B200", "50", 2)),
                List.of());

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        // 2 free A100 (200) + 1 free B200 (50)
        assertThat(result.get().getAmount()).isEqualByComparingTo("250.00");
    }

    @Test
    void getPromotionType_isBuy2Get1Free() {
        assertThat(strategy.getPromotionType()).isEqualTo(PromotionType.BUY2_GET1_FREE);
    }
}
