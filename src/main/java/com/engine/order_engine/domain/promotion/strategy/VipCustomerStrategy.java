package com.engine.order_engine.domain.promotion.strategy;

import java.math.BigDecimal;
import java.util.Optional;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.PromotionType;

public class VipCustomerStrategy implements PromotionStrategy {

    private final PromotionType promotionType = PromotionType.VIP_DISCOUNT;
    private final BigDecimal vipPercentageDiscount = BigDecimal.valueOf(5);

    @Override
    public PromotionType getPromotionType() {
        return this.promotionType;
    }

    @Override
    public Optional<PromotionDetail> apply(PromotionContext context) {
        CustomerType customerType = context.getCustomerType();
        if(!PromotionPipeline.isVipCustomer(customerType)) {
            return Optional.empty();
        }

        BigDecimal discountValue = PromotionPipeline.percentageOf(context.getSubTotal(), this.vipPercentageDiscount);
        PromotionDetail promotion = PromotionDetail.builder().amount(discountValue).discountType(promotionType.name()).build();
        context.applyDiscount(promotion);
        return Optional.of(promotion);
    }
}
