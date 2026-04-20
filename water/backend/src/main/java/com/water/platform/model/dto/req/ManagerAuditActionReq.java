package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "管理员审核操作请求")
@Data
public class ManagerAuditActionReq {

    @NotNull(message = "审核申请ID不能为空")
    @ApiModelProperty(value = "审核申请ID")
    private Long id;

    @ApiModelProperty(value = "管理员审核备注")
    private String auditRemark;
}
