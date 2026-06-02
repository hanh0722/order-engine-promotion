package com.engine.order_engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.engine.order_engine.entity.CouponEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Long> {
    
    public CouponEntity findByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from coupons c where lower(c.code) = lower(:code)")
    CouponEntity findForUpdate(@Param("code") String code);

    @Modifying
    @Query("update coupons c " +
        "set c.quantity = c.quantity - 1 " +
        "where lower(c.code) = lower(:code) " +
        "and c.quantity > 0")
    int consumeCoupon(@Param("code") String code);
}
