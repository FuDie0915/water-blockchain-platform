package com.water.platform.base.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页通用返回类
 */
@Data
@ApiModel(description = "分页基础输出模型")
public class PageResponse<T> implements Serializable {

    @ApiModelProperty(value = "业务状态码")
    private int code;

    @ApiModelProperty(value = "输出数据")
    private List<T> data;

    @ApiModelProperty(value = "消息")
    private String message;

    @ApiModelProperty(value = "总记录数")
    private long total;

    @ApiModelProperty(value = "每页数据量")
    private long size;

    @ApiModelProperty(value = "当前第几页")
    private long current;

    public PageResponse(int code, List<T> data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public PageResponse(int code, List<T> data) {
        this(code, data, "操作成功");
    }

    public PageResponse(int code, long total, long current, long size, List<T> data) {
        this.code = code;
        this.total = total;
        this.current = current;
        this.size = size;
        this.data = data;
    }

    public PageResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    public  PageResponse(T t) {

    }
}
