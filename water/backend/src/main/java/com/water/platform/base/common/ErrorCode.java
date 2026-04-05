package com.water.platform.base.common;

import com.water.platform.utils.FileUploadUtils;

/**
 * 自定义错误码
 *
 */
public enum ErrorCode {



    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    HEADER_PARAMS_ERROR(40001, "请求头参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    INVALID_USER(40102, "用户不存在"),
    USER_LOCKED(40103, "用户未激活"),
    USER_EXISTS(40104, "账户已存在"),

    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    FILE_SIZE_ERROR(60001, "文件上传大小超出限制,最大限制为: 50m"),
    FILE_NAME_ERROR(60002, "文件名称长度超出最大限制,最大限制为:" + FileUploadUtils.DEFAULT_FILE_NAME_LENGTH),
    FILE_UPLOAD_ERROR(60003, "上传文件类型异常"),

    CHAIN_CALL_FAILED(70003, "区块链调用失败"),

    BIND_FAILED(20001, "合约绑定失败"),
    VERIFY_FAILED(20002, "无可审核许可证内容");

    /**
     * 状态码
     */
    private final int code;


    /**
     * 信息
     */
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
