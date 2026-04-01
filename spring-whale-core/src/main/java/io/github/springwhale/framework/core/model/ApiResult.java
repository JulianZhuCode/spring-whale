package io.github.springwhale.framework.core.model;

import lombok.Data;

@Data
public class ApiResult<T> {
    private String code = "200";
    private String message = "success";
    private T data;

    public static ApiResult<Boolean> success() {
        return ApiResult.success(true);
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setData(data);
        return apiResult;
    }

    public static ApiResult<Boolean> error(String code, String message) {
        return ApiResult.error(code, message, false);
    }

    public static <T> ApiResult<T> error(String code, String message, T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(code);
        apiResult.setMessage(message);
        apiResult.setData(data);
        return apiResult;
    }
}
