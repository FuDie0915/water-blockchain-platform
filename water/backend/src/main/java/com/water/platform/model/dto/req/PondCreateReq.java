package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "新增养殖池请求")
@Data
public class PondCreateReq {

    @NotBlank(message = "塘名不能为空")
    @ApiModelProperty(value = "塘名")
    private String pondName;

    @NotBlank(message = "养殖品种不能为空")
    @ApiModelProperty(value = "养殖品种")
    private String breedType;

    @NotNull(message = "养殖面积不能为空")
    @ApiModelProperty(value = "养殖面积（亩）")
    private Double area;

    @NotNull(message = "水深不能为空")
    @ApiModelProperty(value = "水深（米）")
    private Double depth;

    @NotNull(message = "养殖周期开始时间不能为空")
    @ApiModelProperty(value = "养殖周期开始时间")
    private Date startDate;

    @NotNull(message = "养殖周期结束时间不能为空")
    @ApiModelProperty(value = "养殖周期结束时间")
    private Date endDate;

    @NotNull(message = "池塘状态不能为空")
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
}
