package com.shuking.pairBackend.common;

/**
 * 自定义异常枚举类
 */
public enum ErrorCode {
    SUCCESS(200, "ok", ""),
    PARAMS_ERROR(400, "请求参数错误", ""),
    NULL_ERROR(401, "请求数据为空", ""),
    NOT_LOGIN(402, "未登录", ""),
    NO_AUTH(403-1, "无权限", ""),
    FORBIDDEN(403-2, "禁止操作", ""),
    SYSTEM_ERROR(500, "系统内部异常", "");

    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

}
