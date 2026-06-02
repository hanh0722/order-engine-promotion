package com.engine.order_engine.api.service.implement;

import java.math.BigDecimal;
import java.util.List;

import com.engine.order_engine.api.dto.request.promotion.CreatePromotionRequest;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.entity.PromotionEntity;

public interface PromotionService {
    public List<PromotionEntity> getListActivePromotions();
    public PromotionEntity createPromotion(CreatePromotionRequest request);
    public BigDecimal getTotalDiscount(List<PromotionDetail> promotions);
}
