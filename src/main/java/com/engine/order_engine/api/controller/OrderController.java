package com.engine.order_engine.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.dto.response.BaseResponse;
import com.engine.order_engine.api.dto.response.order.CreateOrderResponse;
import com.engine.order_engine.api.service.implement.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/calculate")
    public ResponseEntity<BaseResponse<CreateOrderResponse>> persistOrders(@Valid @RequestBody CalculateOrderRequest request) {
        CreateOrderResponse response = this.orderService.calculate(request);
        return ResponseEntity.ok(BaseResponse.success(response));
    }
}
