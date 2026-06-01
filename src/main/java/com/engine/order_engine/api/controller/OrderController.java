package com.engine.order_engine.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.engine.order_engine.api.dto.request.orders.CalculateOrderRequest;
import com.engine.order_engine.api.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/calculate")
    public ResponseEntity<?> persistOrders(@Valid @RequestBody CalculateOrderRequest request) {
        return ResponseEntity.ok(this.orderService.calculate(request));
    }
}
