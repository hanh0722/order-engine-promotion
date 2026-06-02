package com.engine.order_engine.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.engine.order_engine.domain.customer.CustomerType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Table
@Entity(name = "orders")
@Getter
@Setter
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType = CustomerType.REGULAR;

    @Column(nullable = false, name = "sub_total")
    private BigDecimal subTotal;

    @Column(nullable = false, name = "total_discount")
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    @Column(nullable = false, name = "final_price")
    private BigDecimal finalPrice;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @Version
    private Long version;
}
