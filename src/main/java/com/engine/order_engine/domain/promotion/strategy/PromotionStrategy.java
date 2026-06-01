package com.engine.order_engine.domain.promotion.strategy;

import java.util.List;
import java.util.Optional;

import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionType;

public interface PromotionStrategy {

    // method define to get supported type of strategy
    PromotionType getPromotionType();

    Optional<PromotionDetail> apply(PromotionContext context);
}
