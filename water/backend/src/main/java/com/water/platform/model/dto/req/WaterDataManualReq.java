package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ApiModel(description = "养殖户手动上报水质数据请求")
@Data
public class WaterDataManualReq {

    @NotNull(message = "塘号不能为空")
    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @NotNull(message = "数据类型不能为空")
    @ApiModelProperty(value = "数据类型(2:水温, 3:盐度, 4:pH, 5:溶解氧, 6:氨氮, 7:亚硝酸盐)")
    private Integer dataType;

    @NotBlank(message = "数据值不能为空")
    @ApiModelProperty(value = "数据值")
    private String data;
}
