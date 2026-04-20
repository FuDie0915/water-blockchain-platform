package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "养殖户申请绑定监管局请求")
@Data
public class BindApplyReq {

    @NotNull(message = "监管局ID不能为空")
    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;
}
