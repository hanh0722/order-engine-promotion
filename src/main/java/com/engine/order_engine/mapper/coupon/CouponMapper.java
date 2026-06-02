package com.engine.order_engine.mapper.coupon;

import org.springframework.stereotype.Component;

import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.entity.CouponEntity;

@Component
public class CouponMapper {

    public Coupon toDomain(CouponEntity couponEntity) {
        return Coupon.builder().active(couponEntity.getActive()).code(couponEntity.getCode())
                .expiryDate(couponEntity.getExpiryDate()).discountAmount(couponEntity.getDiscountAmount())
                .quantity(couponEntity.getQuantity())
                .build();
    }

    public CouponEntity toEntity(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.setCode(coupon.getCode());
        entity.setActive(coupon.getActive());
        entity.setDiscountAmount(coupon.getDiscountAmount());
        entity.setExpiryDate(coupon.getExpiryDate());
        entity.setQuantity(coupon.getQuantity());
        return entity;
    }
}
