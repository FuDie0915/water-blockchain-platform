package com.water.platform.base.exception;

import com.water.platform.base.common.ErrorCode;

/**
 * @description: 文件校验异常
 **/

public class InvalidExtensionException extends RuntimeException{


    /**
     * 错误码
     */
    private final int code;

    public InvalidExtensionException(int code, String message) {
        super(message);
        this.code = code;
    }

    public InvalidExtensionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public InvalidExtensionException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
