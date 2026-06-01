package com.engine.order_engine.domain.promotion.strategy;

import java.math.BigDecimal;
import java.util.Optional;

import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.domain.promotion.PromotionType;

public class PercentageDiscountStrategy implements PromotionStrategy {
    
    private final PromotionType type = PromotionType.PERCENTAGE_DISCOUNT;

    @Override
    public PromotionType getPromotionType() {
        return this.type;
    }

    @Override
    public Optional<PromotionDetail> apply(PromotionContext context) {
        BigDecimal totalDiscount = context.getActivePromotions().stream().filter(item -> {
            return item.getType().equals(this.type);
        }).map(item -> {
            BigDecimal discountPercent = item.getValue();
            return PromotionPipeline.percentageOf(context.getSubTotal(), discountPercent);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if(totalDiscount.equals(BigDecimal.ZERO)) {
            return Optional.empty();
        }
        PromotionDetail promotion = PromotionDetail.builder().amount(totalDiscount).discountType(type.name()).build();
        context.applyDiscount(promotion);
        return Optional.of(promotion);
    }
}
