package com.engine.order_engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.engine.order_engine.api.dto.response.BaseError;
import com.engine.order_engine.api.dto.response.BaseResponse;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> handleBusinessException(BusinessException exception) {
        HttpStatus status = exception.getHttpStatusCode() == null ? HttpStatus.BAD_REQUEST
                : exception.getHttpStatusCode();

        return ResponseEntity.status(status)
                .body(BaseResponse.error(new BaseError(exception.getMessage(), exception.getCode())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleGeneralException(Exception exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = exception.getMessage();

        return ResponseEntity.status(status).body(BaseResponse.error(new BaseError(message, null)));
    }
}
