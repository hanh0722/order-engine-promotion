package com.engine.order_engine.api.service.implement;

import java.math.BigDecimal;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.dto.response.order.CreateOrderResponse;
import com.engine.order_engine.entity.OrderEntity;

public interface OrderService {
    public CreateOrderResponse calculate(CalculateOrderRequest request);
    public OrderEntity createOrder(CalculateOrderRequest request, BigDecimal subTotal, BigDecimal finalPrice, BigDecimal totalDiscount);
}
