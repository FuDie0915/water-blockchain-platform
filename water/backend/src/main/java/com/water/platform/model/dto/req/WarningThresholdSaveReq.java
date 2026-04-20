package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "预警阈值修改请求（只传需要修改的字段）")
@Data
public class WarningThresholdSaveReq {

    @ApiModelProperty(value = "水温下限(℃)")
    private Double waterTempMin;

    @ApiModelProperty(value = "水温上限(℃)")
    private Double waterTempMax;

    @ApiModelProperty(value = "盐度下限")
    private Double salinityMin;

    @ApiModelProperty(value = "盐度上限")
    private Double salinityMax;

    @ApiModelProperty(value = "pH下限")
    private Double phMin;

    @ApiModelProperty(value = "pH上限")
    private Double phMax;

    @ApiModelProperty(value = "溶解氧下限(mg/L)")
    private Double doMin;

    @ApiModelProperty(value = "溶解氧上限(mg/L)")
    private Double doMax;

    @ApiModelProperty(value = "氨氮下限(mg/L)")
    private Double nh3nMin;

    @ApiModelProperty(value = "氨氮上限(mg/L)")
    private Double nh3nMax;

    @ApiModelProperty(value = "亚硝酸盐下限(mg/L)")
    private Double no2Min;

    @ApiModelProperty(value = "亚硝酸盐上限(mg/L)")
    private Double no2Max;
}
