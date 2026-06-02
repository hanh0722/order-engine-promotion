package com.engine.order_engine.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Table
@Entity(name = "coupons")
@Getter
@Setter
public class CouponEntity {
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

    @Column(nullable = false)
    private Integer quantity;

    @Version
    private Long version;
}
