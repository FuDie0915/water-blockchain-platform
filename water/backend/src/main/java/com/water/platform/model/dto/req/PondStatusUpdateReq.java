package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "更新养殖池状态请求")
@Data
public class PondStatusUpdateReq {

    @NotNull(message = "养殖池ID不能为空")
    @ApiModelProperty(value = "养殖池ID")
    private Long id;

    @NotNull(message = "池塘状态不能为空")
    @ApiModelProperty(value = "池塘状态（0=空闲, 1=养殖中, 2=休整）")
    private Integer status;
}
