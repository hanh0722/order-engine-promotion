package com.engine.order_engine.domain.promotion.strategy;

import static com.engine.order_engine.support.PromotionTestFixtures.context;
import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.promotion.PromotionType;

class VipCustomerStrategyTest {

    private VipCustomerStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new VipCustomerStrategy();
    }

    @Test
    void apply_appliesFivePercentForVipCustomer() {
        var promotionContext = context(
                CustomerType.VIP,
                null,
                List.of(item("A100", "100", 2), item("B200", "50", 1)),
                List.of());

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountType()).isEqualTo("VIP_DISCOUNT");
        assertThat(result.get().getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void apply_returnsEmptyForRegularCustomer() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("A100", "100", 2)),
                List.of());

        assertThat(strategy.apply(promotionContext)).isEmpty();
    }

    @Test
    void getPromotionType_isVipDiscount() {
        assertThat(strategy.getPromotionType()).isEqualTo(PromotionType.VIP_DISCOUNT);
    }
}
