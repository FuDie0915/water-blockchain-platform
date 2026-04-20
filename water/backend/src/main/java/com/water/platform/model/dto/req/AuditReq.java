package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "审核请求")
@Data
public class AuditReq {

    @NotNull(message = "记录ID不能为空")
    @ApiModelProperty(value = "记录ID")
    private Long id;

    @NotNull(message = "审核状态不能为空")
    @ApiModelProperty(value = "审核状态（1=通过, 2=拒绝）")
    private Integer auditStatus;
}
