package com.engine.order_engine.api.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.dto.response.order.CreateOrderResponse;
import com.engine.order_engine.domain.dto.coupon.Coupon;
import com.engine.order_engine.domain.dto.promotion.Promotion;
import com.engine.order_engine.domain.model.PromotionContext;
import com.engine.order_engine.domain.model.PromotionDetail;
import com.engine.order_engine.domain.promotion.PromotionPipeline;
import com.engine.order_engine.entity.CouponEntity;
import com.engine.order_engine.entity.OrderEntity;
import com.engine.order_engine.entity.OrderItemEntity;
import com.engine.order_engine.entity.PromotionEntity;
import com.engine.order_engine.exception.BusinessException;
import com.engine.order_engine.exception.coupon.CouponStatusMessage;
import com.engine.order_engine.mapper.coupon.CouponMapper;
import com.engine.order_engine.mapper.promotion.PromotionMapper;
import com.engine.order_engine.repository.CouponRepository;
import com.engine.order_engine.repository.OrderRepository;
import com.engine.order_engine.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final PromotionPipeline promotionPipeline;
    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final OrderRepository orderRepository;
    private final CouponMapper couponMapper;
    private final PromotionMapper promotionMapper;

    @Transactional
    public CreateOrderResponse calculate(CalculateOrderRequest request) {
        String couponCode = request.getCouponCode();
        Coupon coupon = null;
        CouponEntity couponEntity;
        // check coupon
        if (couponCode != null) {
            couponEntity = this.couponRepository.findForUpdate(request.getCouponCode());
            if (couponEntity == null) {
                throw new BusinessException("Coupon " + couponCode + " is not existed",
                        CouponStatusMessage.COUPON_NOT_FOUND.name());
            }
            if (!couponEntity.getActive()) {
                throw new BusinessException("Coupon " + couponCode + " is not valid",
                        CouponStatusMessage.COUPON_INVALID.name());
            }
            if(couponEntity.getQuantity() <= 0) {
                throw new BusinessException("Coupon " + couponCode + " is already used maximum", CouponStatusMessage.COUPON_INVALID.name());
            }
            coupon = this.couponMapper.toDomain(couponEntity);
            couponEntity.setQuantity(couponEntity.getQuantity() - 1);
        }
        // Get list active promotions
        List<PromotionEntity> promotions = this.promotionRepository.findByActiveTrue();

        // initialize context for pipeline
        PromotionContext context = new PromotionContext(request.getCustomerType(), coupon,
                request.getItems(),
                this.promotionMapper.toListDomain(promotions));
        // Process pipeline for strategy
        List<PromotionDetail> appliedPromotions = this.promotionPipeline.process(context);

        // Get total discount based on promotions applied of pipeline
        BigDecimal totalDiscount = this.promotionService.getTotalDiscount(appliedPromotions);

        // calculate final price of order
        BigDecimal finalPrice = context.getSubTotal().subtract(totalDiscount);
    
        // create an order
        OrderEntity order = this.createOrder(request, context.getSubTotal(), finalPrice, totalDiscount);
    
        return CreateOrderResponse.builder().subTotal(context.getSubTotal()).orderId(order.getId())
                .discounts(appliedPromotions).totalDiscount(totalDiscount).finalPrice(finalPrice).build();
    }

    public OrderEntity createOrder(CalculateOrderRequest request, BigDecimal subTotal, BigDecimal finalPrice,
            BigDecimal totalDiscount) {
        OrderEntity order = new OrderEntity();
        List<OrderItemEntity> orderItems = request.getItems().stream().map(item -> {
            OrderItemEntity orderItem = new OrderItemEntity();
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
        order.setTotalDiscount(totalDiscount);

        return this.orderRepository.save(order);
    }
}
