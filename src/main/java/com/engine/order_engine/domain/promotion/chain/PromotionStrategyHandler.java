package com.engine.order_engine.domain.promotion.chain;

import java.util.List;
import java.util.Optional;

import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.strategy.PromotionStrategy;
import com.engine.order_engine.entity.Promotion;

public class PromotionStrategyHandler extends PromotionHandler {

    private final PromotionStrategy strategy;

    public PromotionStrategyHandler(PromotionStrategy strategy) {
        this.strategy = strategy;
    }
    
    @Override
    protected Boolean supports(PromotionContext context) {
        List<Promotion> promotions = context.getActivePromotions();
        return promotions.stream().anyMatch(item -> item.getType().equals(this.strategy.getPromotionType()));
    }

    @Override
    protected Optional<PromotionDetail> doHandle(PromotionContext context) {
        return this.strategy.apply(context);
    }
}
