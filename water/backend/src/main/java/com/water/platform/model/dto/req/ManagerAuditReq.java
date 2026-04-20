package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@ApiModel(description = "监管局提交审核申请请求")
@Data
public class ManagerAuditReq {

    @NotBlank(message = "机构名称不能为空")
    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @NotBlank(message = "管辖区域不能为空")
    @ApiModelProperty(value = "管辖区域")
    private String jurisdiction;

    @NotBlank(message = "联系电话不能为空")
    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "申请备注")
    private String remark;
}
