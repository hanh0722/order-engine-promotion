package com.engine.order_engine.mapper.promotion;

import java.util.List;

import org.springframework.stereotype.Component;

import com.engine.order_engine.domain.dto.promotion.Promotion;
import com.engine.order_engine.entity.PromotionEntity;

@Component
public class PromotionMapper {

    public Promotion toDomain(PromotionEntity entity) {
        return Promotion.builder().active(entity.getActive()).type(entity.getType()).value(entity.getValue())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt())
                .build();
    }

    public PromotionEntity toEntity(Promotion promotion) {
        PromotionEntity entity = new PromotionEntity();
        entity.setActive(promotion.getActive());
        entity.setCreatedAt(promotion.getCreatedAt());
        entity.setUpdatedAt(promotion.getUpdatedAt());
        entity.setType(promotion.getType());
        entity.setValue(promotion.getValue());
        return entity;
    }

    public List<Promotion> toListDomain(List<PromotionEntity> entities) {
        return entities.stream().map(item -> {
            return toDomain(item);
        }).toList();
    }
}
