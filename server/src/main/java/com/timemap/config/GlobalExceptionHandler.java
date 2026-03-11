package com.timemap.config;

import com.timemap.common.Result;
import com.timemap.monitor.BusinessMetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final BusinessMetricsCollector metricsCollector;

    @ExceptionHandler(com.timemap.common.BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(com.timemap.common.BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getCustomMessage());
        metricsCollector.recordBusinessError("business_" + e.getCode());
        return Result.fail(e.getCode(), e.getCustomMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        metricsCollector.recordBusinessError("runtime");
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        metricsCollector.recordBusinessError("max_upload_size");
        return Result.fail(com.timemap.common.ErrorCode.PHOTO_SIZE_EXCEEDED);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        metricsCollector.recordBusinessError("system");
        return Result.fail(com.timemap.common.ErrorCode.SYSTEM_ERROR);
    }
}
