package com.engine.order_engine.domain.dto.promotion;

import java.math.BigDecimal;
import java.time.Instant;

import com.engine.order_engine.domain.promotion.PromotionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Promotion {
    private PromotionType type;
    private BigDecimal value;
    private Boolean active;
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
