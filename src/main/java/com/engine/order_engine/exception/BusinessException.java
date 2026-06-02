package com.engine.order_engine.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatusCode;
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.httpStatusCode = null;
        this.code = null;
    }

    public BusinessException(String message, String code) {
        super(message);
        this.httpStatusCode = null;
        this.code = code;
    }
}
