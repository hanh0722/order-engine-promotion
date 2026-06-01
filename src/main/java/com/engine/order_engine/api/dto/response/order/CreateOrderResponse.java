package com.engine.order_engine.api.dto.response.order;

import java.math.BigDecimal;
import java.util.List;

import com.engine.order_engine.domain.model.PromotionDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateOrderResponse {
    private Long orderId;
    private BigDecimal subTotal;
    private List<PromotionDetail> discounts;
    private BigDecimal totalDiscount;
    private BigDecimal finalPrice;
}
