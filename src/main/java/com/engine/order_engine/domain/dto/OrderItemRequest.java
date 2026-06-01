package com.engine.order_engine.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemRequest {
    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
}
