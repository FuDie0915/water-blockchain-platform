package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@TableName(value = "warning_threshold")
@ApiModel(description = "预警阈值配置")
@Data
public class WarningThreshold {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;

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

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
