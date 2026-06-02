package com.engine.order_engine.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.dto.OrderItemRequest;
import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.domain.dto.promotion.Promotion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class PromotionContext {
    private CustomerType customerType;
    private Coupon coupon;
    private List<OrderItemRequest> items;
    private BigDecimal subTotal;
    private List<Promotion> activePromotions;
    private List<PromotionDetail> appliedDiscount = new ArrayList<>();

    public PromotionContext(CustomerType customerType, Coupon coupon, List<OrderItemRequest> items, List<Promotion> activePromotions) {
        this.customerType = customerType;
        this.coupon = coupon;
        this.items = List.copyOf(items);
        this.activePromotions = activePromotions;
        this.subTotal = this.calculateSubTotal(items);
    }

    private BigDecimal calculateSubTotal(List<OrderItemRequest> listItems) {
        BigDecimal total = listItems.stream().map(item -> {
            return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    public void applyDiscount(PromotionDetail promotion) {
        this.appliedDiscount.add(promotion);
    }
}
