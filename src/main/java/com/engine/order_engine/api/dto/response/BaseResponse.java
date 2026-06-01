package com.engine.order_engine.api.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class BaseResponse<T> {

    private T data;
    private BaseError error;
    
    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();

        response.setData(data);
        response.setError(null);

        return response;
    }

    public static <T> BaseResponse<T> error(BaseError error) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setData(null);
        response.setError(error);

        return response;
    }
}
