package com.engine.order_engine.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.api.service.implement.PromotionService;
import com.engine.order_engine.domain.dto.promotion.Promotion;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.mapper.promotion.PromotionMapper;
import com.engine.order_engine.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
// annotation to create constructor
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public List<Promotion> getListActivePromotions() {
        List<PromotionEntity> promotions = this.promotionRepository.findByActiveTrue();
        List<Promotion> listPromotions = promotions.stream().map(item -> this.promotionMapper.toDomain(item)).toList();
        return listPromotions;
    }

    public Promotion createPromotion(CreatePromotionRequest request) {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setType(request.getType());
        promotion.setActive(request.isActive());
        promotion.setValue(request.getValue());
        PromotionEntity entity = this.promotionRepository.save(promotion);
        return this.promotionMapper.toDomain(entity);
    }

    public BigDecimal getTotalDiscount(List<PromotionDetail> promotions) {
        return promotions.stream().map(item -> {
            return item.getAmount();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
