package com.engine.order_engine.domain.promotion.strategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionType;
import com.engine.order_engine.entity.Coupon;
import com.engine.order_engine.exception.BusinessException;

public class CouponStrategy implements PromotionStrategy {

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.COUPON_FIXED;
    }

    @Override
    public Optional<PromotionDetail> apply(PromotionContext context) {
        Coupon coupon = context.getCoupon();
        if(coupon == null) {
            return Optional.empty();
        }
        Boolean isActiveCoupon = coupon.getActive();
        if(!isActiveCoupon) {
            throw new BusinessException("Coupon " + coupon + " is not activated");
        }
        if(coupon.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException("Coupon " + coupon + " is expired");
        }

        BigDecimal discountAmount = coupon.getDiscountAmount();
        PromotionDetail promotion = PromotionDetail.builder().amount(discountAmount).discountType("COUPON_" + coupon.getCode()).build();
        context.applyDiscount(promotion);
        return Optional.of(promotion);
    }
}
