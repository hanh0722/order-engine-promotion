package com.engine.order_engine.api.dto.request.orders;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ItemLineRequest {
    @NotBlank(message = "SKU is required")
    String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be higher than 0")
    BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be higher than 0")
    Integer quantity;
}
