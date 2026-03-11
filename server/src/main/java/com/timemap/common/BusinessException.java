package com.timemap.common;

/**
 * 业务异常类
 * 用于抛出带有错误码的业务异常
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String customMessage;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    public int getCode() {
        return errorCode.getCode();
    }
    
    public String getCustomMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
