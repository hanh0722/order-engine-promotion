package com.engine.order_engine.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.entity.Promotion;
import com.engine.order_engine.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
// annotation to create constructor
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public List<Promotion> getListActivePromotions() {
        return this.promotionRepository.findByActiveTrue();
    }

    public Promotion createPromotion(CreatePromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setType(request.getType());
        promotion.setActive(request.isActive());
        promotion.setValue(request.getValue());
        return this.promotionRepository.save(promotion);
    }

    public BigDecimal getTotalDiscount(List<PromotionDetail> promotions) {
        return promotions.stream().map(item -> {
            return item.getAmount();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
