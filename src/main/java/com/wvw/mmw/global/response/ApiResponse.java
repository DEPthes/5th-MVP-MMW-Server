package com.wvw.mmw.global.response;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    private static final String SUCCESS_CODE = "SUCCESS";

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, message, data);
    }
}
