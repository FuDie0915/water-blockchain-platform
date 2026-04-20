package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author ：devon
 * @date ：2024/11/14 14:13
 * @description：水数据采集入参
 * @version: 1.0
 */
@ApiModel(description = "水数据采集入参")
@Data
public class WaterDataCollectReq {

    @ApiModelProperty(value = "用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @ApiModelProperty(value = "数据类型(0:TDS、1:浑浊)", example = "0")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @ApiModelProperty(value = "水质状态(正常、异常)")
    @NotNull(message = "水质状态不能为空")
    private String status;

    @ApiModelProperty(value = "水数据")
    @NotBlank(message = "水数据不能为空")
    private String data;
}
