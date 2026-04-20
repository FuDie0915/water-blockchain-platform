package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "更新养殖池请求")
@Data
public class PondUpdateReq {

    @NotNull(message = "养殖池ID不能为空")
    @ApiModelProperty(value = "养殖池ID")
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
}
