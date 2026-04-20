package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "绑定状态响应")
@Data
public class BindStatusResp {

    @ApiModelProperty(value = "绑定记录ID")
    private Long id;

    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;

    @ApiModelProperty(value = "监管局名称")
    private String managerName;

    @ApiModelProperty(value = "状态（0=待审核, 1=已通过, 2=已拒绝）")
    private Integer status;
}
