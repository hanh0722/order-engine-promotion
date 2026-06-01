package com.engine.order_engine.domain.promotion.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.engine.order_engine.domain.dto.OrderItemRequest;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionType;

public class Buy2Get1Strategy implements PromotionStrategy {

    private final PromotionType type = PromotionType.BUY2_GET1_FREE;

    @Override
    public PromotionType getPromotionType() {
        return this.type;
    }

    @Override
    public Optional<PromotionDetail> apply(PromotionContext context) {
        List<OrderItemRequest> items = context.getItems();
        BigDecimal totalDiscount = items.stream().map(item -> {
            Integer quantity = item.getQuantity();
            Integer freeUnit = quantity / 2;
            return item.getPrice().multiply(BigDecimal.valueOf(freeUnit));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        String promotionType = type.name();
        PromotionDetail promotion = PromotionDetail.builder().discountType(promotionType).amount(totalDiscount).build();
        context.applyDiscount(promotion);
        return Optional.of(PromotionDetail.builder().discountType(promotionType).amount(totalDiscount).build());
    }
}
