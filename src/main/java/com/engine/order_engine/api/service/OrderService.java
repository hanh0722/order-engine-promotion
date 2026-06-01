package com.engine.order_engine.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.dto.response.order.CreateOrderResponse;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.entity.Coupon;
import com.engine.order_engine.entity.Order;
import com.engine.order_engine.entity.OrderItem;
import com.engine.order_engine.entity.Promotion;
import com.engine.order_engine.exception.GeneralException;
import com.engine.order_engine.repository.CouponRepository;
import com.engine.order_engine.repository.OrderRepository;
import com.engine.order_engine.repository.PromotionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final PromotionPipeline promotionPipeline;
    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final OrderRepository orderRepository;

    @Transactional
    public CreateOrderResponse calculate(CalculateOrderRequest request) {
        String couponCode = request.getCouponCode();
        Coupon coupon = null;
        if (couponCode != null) {
            coupon = this.couponRepository.findByCodeAndActiveTrue(request.getCouponCode());
            if (coupon == null) {
                throw new GeneralException("Coupon " + couponCode + " is not valid");
            }
        }
        List<Promotion> promotions = this.promotionRepository.findByActiveTrue();
        PromotionContext context = new PromotionContext(request.getCustomerType(), coupon, request.getItems(),
                promotions);
        List<PromotionDetail> appliedPromotions = this.promotionPipeline.process(context);
        BigDecimal totalDiscount = this.promotionService.getTotalDiscount(appliedPromotions);
        BigDecimal finalPrice = context.getSubTotal().subtract(totalDiscount);
        Order order = this.createOrder(request, context.getSubTotal(), finalPrice, totalDiscount);
        return CreateOrderResponse.builder().subTotal(context.getSubTotal()).orderId(order.getId())
                .discounts(appliedPromotions).totalDiscount(totalDiscount).finalPrice(finalPrice).build();
    }

    public Order createOrder(CalculateOrderRequest request, BigDecimal subTotal, BigDecimal finalPrice, BigDecimal totalDiscount) {
        Order order = new Order();
        List<OrderItem> orderItems = request.getItems().stream().map(item -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSku(item.getSku());
            orderItem.setOrder(order);
            return orderItem;
        }).toList();

        order.setSubTotal(subTotal);
        order.setItems(orderItems);
        order.setCustomerType(request.getCustomerType());
        order.setFinalPrice(finalPrice);

        return this.orderRepository.save(order);
    }
}
