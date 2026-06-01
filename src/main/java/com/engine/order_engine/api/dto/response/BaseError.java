package com.engine.order_engine.api.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BaseError {
    private String message;
    private String code;

    public BaseError(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
