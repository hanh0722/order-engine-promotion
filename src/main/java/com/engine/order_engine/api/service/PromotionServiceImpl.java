package com.engine.order_engine.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.api.service.implement.PromotionService;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
// annotation to create constructor
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;

    public List<PromotionEntity> getListActivePromotions() {
        return this.promotionRepository.findByActiveTrue();
    }

    public PromotionEntity createPromotion(CreatePromotionRequest request) {
        PromotionEntity promotion = new PromotionEntity();
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
