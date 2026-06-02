package com.engine.order_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.engine.order_engine.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    public Coupon findByCodeAndActiveTrue(String code);

    public Coupon findByCode(String code);
}
