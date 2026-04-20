package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "养殖池响应")
@Data
public class PondResp {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "塘名")
    private String pondName;

    @ApiModelProperty(value = "养殖品种")
    private String breedType;

    @ApiModelProperty(value = "养殖面积（亩）")
    private Double area;

    @ApiModelProperty(value = "水深（米）")
    private Double depth;

    @ApiModelProperty(value = "养殖周期开始时间")
    private Date startDate;

    @ApiModelProperty(value = "养殖周期结束时间")
    private Date endDate;

    @ApiModelProperty(value = "池塘状态（0=空闲, 1=养殖中, 2=休整）")
    private Integer status;

    @ApiModelProperty(value = "水温测量值")
    private Double waterTemp;

    @ApiModelProperty(value = "盐度测量值")
    private Double salinity;

    @ApiModelProperty(value = "pH测量值")
    private Double ph;

    @ApiModelProperty(value = "溶解氧测量值")
    private Double doValue;

    @ApiModelProperty(value = "氨氮测量值")
    private Double nh3n;

    @ApiModelProperty(value = "亚硝酸盐测量值")
    private Double no2;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
