package com.engine.order_engine.api.dto.request.promotion;

import java.math.BigDecimal;

import com.engine.order_engine.domain.promotion.PromotionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreatePromotionRequest {
    @NotNull(message = "Promotion type is required")
    PromotionType type;

    @NotNull(message = "Value of promotion is required")
    @DecimalMin(value = "0.01", message = "Value of promotion must be higher than 0")
    BigDecimal value;

    @NotNull(message = "Active is required")
    boolean active;
}
