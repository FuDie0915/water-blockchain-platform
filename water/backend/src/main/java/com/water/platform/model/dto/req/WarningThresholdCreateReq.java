package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "预警阈值新建请求（全部参数必填）")
@Data
public class WarningThresholdCreateReq {

    @NotNull(message = "水温下限不能为空")
    @ApiModelProperty(value = "水温下限(℃)")
    private Double waterTempMin;

    @NotNull(message = "水温上限不能为空")
    @ApiModelProperty(value = "水温上限(℃)")
    private Double waterTempMax;

    @NotNull(message = "盐度下限不能为空")
    @ApiModelProperty(value = "盐度下限")
    private Double salinityMin;

    @NotNull(message = "盐度上限不能为空")
    @ApiModelProperty(value = "盐度上限")
    private Double salinityMax;

    @NotNull(message = "pH下限不能为空")
    @ApiModelProperty(value = "pH下限")
    private Double phMin;

    @NotNull(message = "pH上限不能为空")
    @ApiModelProperty(value = "pH上限")
    private Double phMax;

    @NotNull(message = "溶解氧下限不能为空")
    @ApiModelProperty(value = "溶解氧下限(mg/L)")
    private Double doMin;

    @NotNull(message = "溶解氧上限不能为空")
    @ApiModelProperty(value = "溶解氧上限(mg/L)")
    private Double doMax;

    @NotNull(message = "氨氮下限不能为空")
    @ApiModelProperty(value = "氨氮下限(mg/L)")
    private Double nh3nMin;

    @NotNull(message = "氨氮上限不能为空")
    @ApiModelProperty(value = "氨氮上限(mg/L)")
    private Double nh3nMax;

    @NotNull(message = "亚硝酸盐下限不能为空")
    @ApiModelProperty(value = "亚硝酸盐下限(mg/L)")
    private Double no2Min;

    @NotNull(message = "亚硝酸盐上限不能为空")
    @ApiModelProperty(value = "亚硝酸盐上限(mg/L)")
    private Double no2Max;
}
