package com.maptrace.common;

import lombok.Getter;

/**
 * 统一错误码枚举
 * 对齐模版规范：0 成功 / 40000 参数错误 / 40100 未登录 / 40300 无权限 / 50000 系统异常
 * 业务错误码在通用码基础上扩展，保持语义清晰
 */
@Getter
public enum ErrorCode {

    // ===== 通用错误码（对齐模版规范） =====
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40300, "无权限"),
    FORBIDDEN_ERROR(40301, "禁止访问"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),

    // ===== 用户相关 (401xx) =====
    USER_NOT_FOUND(40101, "用户不存在"),
    USER_BANNED(40102, "账号已被封禁"),
    USER_MUTED(40103, "你已被禁言"),
    USER_UPLOAD_BANNED(40104, "你已被禁止上传"),

    // ===== 照片相关 (402xx) =====
    PHOTO_NOT_FOUND(40201, "照片不存在"),
    PHOTO_UPLOAD_FAILED(40202, "照片上传失败"),
    PHOTO_UPLOAD_LIMIT(40203, "上传过于频繁，请稍后再试"),
    PHOTO_SIZE_EXCEEDED(40204, "照片大小超出限制"),
    PHOTO_DELETE_FORBIDDEN(40205, "无权删除该照片"),

    // ===== 评论相关 (4031x) =====
    COMMENT_NOT_FOUND(40310, "评论不存在"),
    COMMENT_LIMIT(40311, "评论过于频繁，请稍后再试"),
    COMMENT_DELETE_FORBIDDEN(40312, "无权删除该评论"),

    // ===== 举报相关 (404xx) =====
    REPORT_NOT_FOUND(40401, "举报记录不存在"),
    REPORT_LIMIT(40402, "举报过于频繁，请稍后再试"),
    REPORT_DUPLICATE(40403, "该内容已被举报处理或正在处理中"),
    REPORT_SELF(40404, "不能举报自己发布的内容"),
    REPORT_ALREADY_HANDLED(40405, "该举报已处理，请勿重复操作"),
    REPORT_PERMISSION_LIMITED(40406, "你的举报权限已受限"),

    // ===== 申诉相关 (405xx) =====
    APPEAL_NOT_FOUND(40501, "申诉记录不存在"),
    APPEAL_DUPLICATE(40502, "该违规已提交申诉，请勿重复提交"),
    APPEAL_ALREADY_HANDLED(40503, "该申诉已处理"),

    // ===== 管理员相关 (406xx) =====
    ADMIN_NOT_FOUND(40601, "管理员不存在"),
    ADMIN_LOGIN_FAILED(40602, "用户名或密码错误"),
    ADMIN_ACCOUNT_LOCKED(40603, "账号已被锁定"),
    ADMIN_MUST_CHANGE_PASSWORD(40604, "首次登录需修改密码"),
    ADMIN_PASSWORD_WEAK(40605, "密码强度不足"),
    ADMIN_PASSWORD_REUSED(40606, "不能使用最近3次使用过的密码"),

    // ===== 微信相关 (407xx) =====
    WECHAT_LOGIN_FAILED(40701, "微信登录失败"),
    WECHAT_CODE_INVALID(40702, "微信登录凭证无效");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
