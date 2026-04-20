package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "监管局扩展信息")
@Data
public class ManagerProfileReq {
    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "管辖区域")
    private String jurisdiction;
}
