package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "新增苗种投放请求")
@Data
public class SeedCreateReq {

    @NotNull(message = "塘号不能为空")
    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @NotNull(message = "投放日期不能为空")
    @ApiModelProperty(value = "投放日期")
    private Date recordDate;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "苗种品种")
    private String seedType;

    @ApiModelProperty(value = "苗种规格")
    private String seedSpec;

    @ApiModelProperty(value = "投放重量（kg）")
    private Double weight;

    @ApiModelProperty(value = "备注")
    private String remark;
}
