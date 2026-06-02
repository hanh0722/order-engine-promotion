package com.engine.order_engine.domain.promotion.strategy;

import static com.engine.order_engine.support.PromotionTestFixtures.context;
import static com.engine.order_engine.support.PromotionTestFixtures.item;
import static com.engine.order_engine.support.PromotionTestFixtures.summer10Coupon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.entity.CouponEntity;
import com.engine.order_engine.exception.BusinessException;
import com.engine.order_engine.domain.promotion.PromotionType;

class CouponStrategyTest {

    private CouponStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CouponStrategy();
    }

    @Test
    void apply_appliesFixedCouponDiscount() {
        var promotionContext = context(
                CustomerType.REGULAR,
                summer10Coupon(),
                List.of(item("A100", "100", 1)),
                List.of());

        var result = strategy.apply(promotionContext);

        assertThat(result).isPresent();
        assertThat(result.get().getDiscountType()).isEqualTo("COUPON_SUMMER10");
        assertThat(result.get().getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void apply_returnsEmptyWhenNoCouponInContext() {
        var promotionContext = context(
                CustomerType.REGULAR,
                null,
                List.of(item("A100", "100", 1)),
                List.of());

        assertThat(strategy.apply(promotionContext)).isEmpty();
    }

    @Test
    void apply_throwsWhenCouponIsInactive() {
        Coupon inactive = mock(Coupon.class);
        when(inactive.getActive()).thenReturn(false);
        when(inactive.toString()).thenReturn("SAVE20");

        var promotionContext = context(
                CustomerType.REGULAR,
                inactive,
                List.of(item("A100", "100", 1)),
                List.of());

        assertThatThrownBy(() -> strategy.apply(promotionContext))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not activated");
    }

    @Test
    void apply_throwsWhenCouponIsExpired() {
        Coupon expired = mock(Coupon.class);
        when(expired.getActive()).thenReturn(true);
        when(expired.getExpiryDate()).thenReturn(Instant.parse("2020-01-01T00:00:00Z"));
        when(expired.toString()).thenReturn("SUMMER10");

        var promotionContext = context(
                CustomerType.REGULAR,
                expired,
                List.of(item("A100", "100", 1)),
                List.of());

        assertThatThrownBy(() -> strategy.apply(promotionContext))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void getPromotionType_isCouponFixed() {
        assertThat(strategy.getPromotionType()).isEqualTo(PromotionType.COUPON_FIXED);
    }
}
