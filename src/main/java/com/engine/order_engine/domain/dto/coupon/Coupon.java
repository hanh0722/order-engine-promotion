package com.engine.order_engine.domain.dto.coupon;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Coupon {
    private BigDecimal discountAmount;
    private Boolean active;
    private String code;
    private Instant expiryDate;

    @Builder.Default
    private Integer quantity = 0;
}
