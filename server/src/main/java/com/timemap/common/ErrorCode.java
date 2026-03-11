package com.timemap.common;

/**
 * 业务错误码枚举
 * 格式：模块(2位) + 错误类型(2位) + 序号(2位)
 */
public enum ErrorCode {
    
    // 通用错误 (00xx)
    SUCCESS(0, "成功"),
    SYSTEM_ERROR(1, "系统错误"),
    PARAM_ERROR(2, "参数错误"),
    UNAUTHORIZED(3, "未授权"),
    FORBIDDEN(4, "无权限"),
    NOT_FOUND(5, "资源不存在"),
    
    // 用户相关 (10xx)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_BANNED(1002, "账号已被封禁"),
    USER_MUTED(1003, "你已被禁言"),
    USER_UPLOAD_BANNED(1004, "你已被禁止上传"),
    
    // 照片相关 (20xx)
    PHOTO_NOT_FOUND(2001, "照片不存在"),
    PHOTO_UPLOAD_FAILED(2002, "照片上传失败"),
    PHOTO_UPLOAD_LIMIT(2003, "上传过于频繁，请稍后再试"),
    PHOTO_SIZE_EXCEEDED(2004, "照片大小超出限制"),
    PHOTO_DELETE_FORBIDDEN(2005, "无权删除该照片"),
    
    // 评论相关 (30xx)
    COMMENT_NOT_FOUND(3001, "评论不存在"),
    COMMENT_LIMIT(3002, "评论过于频繁，请稍后再试"),
    COMMENT_DELETE_FORBIDDEN(3003, "无权删除该评论"),
    
    // 举报相关 (40xx)
    REPORT_NOT_FOUND(4001, "举报记录不存在"),
    REPORT_LIMIT(4002, "举报过于频繁，请稍后再试"),
    REPORT_DUPLICATE(4003, "该内容已被举报处理或正在处理中"),
    REPORT_SELF(4004, "不能举报自己发布的内容"),
    REPORT_ALREADY_HANDLED(4005, "该举报已处理，请勿重复操作"),
    REPORT_PERMISSION_LIMITED(4006, "你的举报权限已受限"),
    
    // 申诉相关 (50xx)
    APPEAL_NOT_FOUND(5001, "申诉记录不存在"),
    APPEAL_DUPLICATE(5002, "该违规已提交申诉，请勿重复提交"),
    APPEAL_ALREADY_HANDLED(5003, "该申诉已处理"),
    
    // 管理员相关 (60xx)
    ADMIN_NOT_FOUND(6001, "管理员不存在"),
    ADMIN_LOGIN_FAILED(6002, "用户名或密码错误"),
    ADMIN_ACCOUNT_LOCKED(6003, "账号已被锁定"),
    ADMIN_MUST_CHANGE_PASSWORD(6004, "首次登录需修改密码"),
    ADMIN_PASSWORD_WEAK(6005, "密码强度不足"),
    ADMIN_PASSWORD_REUSED(6006, "不能使用最近3次使用过的密码"),
    
    // 微信相关 (70xx)
    WECHAT_LOGIN_FAILED(7001, "微信登录失败"),
    WECHAT_CODE_INVALID(7002, "微信登录凭证无效");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
