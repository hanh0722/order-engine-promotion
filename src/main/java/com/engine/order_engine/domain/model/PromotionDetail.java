package com.engine.order_engine.domain.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class PromotionDetail {
    private String discountType;
    private BigDecimal amount;
}