package com.maptrace.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体，对齐模版规范：{ code, data, message }
 * 成功码为 0（非 200），错误码使用 ErrorCode 枚举
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private T data;
    private String message;

    private Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), null, errorCode.getMessage());
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, null, message);
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), null, message);
    }
}
