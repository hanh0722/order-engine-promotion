package com.engine.order_engine.api.dto.request.orders;

import java.util.List;

import com.engine.order_engine.domain.customer.CustomerType;
import com.engine.order_engine.domain.dto.OrderItemRequest;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CalculateOrderRequest {
    CustomerType customerType;

    String couponCode;

    @NotEmpty(message = "Items are required")
    List<OrderItemRequest> items;
}
