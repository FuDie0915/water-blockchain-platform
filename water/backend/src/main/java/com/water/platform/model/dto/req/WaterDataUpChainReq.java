package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author ：devon
 * @date ：2024/11/14 14:13
 * @description：水数据上链入参
 * @version: 1.0
 */
@ApiModel(description = "水数据上链入参")
@Data
public class WaterDataUpChainReq {

    @ApiModelProperty(value = "ID", example = "1")
    @NotEmpty(message = "id不能为空")
    private List<Long> id;
}
