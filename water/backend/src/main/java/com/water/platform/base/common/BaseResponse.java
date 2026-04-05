package com.water.platform.base.common;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通用返回类
 *
 */
@Data
@ApiModel(description = "基础输出模型")
public class BaseResponse<T> implements Serializable {

    @ApiModelProperty(value = "业务状态码")
    private int code;

    @ApiModelProperty(value = "输出数据")
    private T data;

    @ApiModelProperty(value = "消息")
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
