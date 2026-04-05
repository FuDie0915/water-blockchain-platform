package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author ：devon
 * @date ：2024/11/14 14:13
 * @description：水数据入参
 * @version: 1.0
 */
@ApiModel(description = "水数据入参")
@Data
public class WaterDataTypeReq {

    @ApiModelProperty(value = "数据类型(0:TDS、1:浑浊)", example = "0")
    @NotNull(message = "数据类型不能为空")
    @Min(0)
    @Max(1)
    private Integer dataType;
}
