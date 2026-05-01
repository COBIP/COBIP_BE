package com.cobip.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    // 실패 응답
    public static ApiResponse<?> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}