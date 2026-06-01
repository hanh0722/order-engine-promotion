package com.engine.order_engine.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Table
@Entity(name = "coupons")
@Getter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Instant expiryDate;
}
