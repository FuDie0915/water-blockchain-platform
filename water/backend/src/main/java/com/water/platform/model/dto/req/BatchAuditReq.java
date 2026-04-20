package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "批量审核请求")
@Data
public class BatchAuditReq {

    @NotEmpty(message = "记录ID列表不能为空")
    @ApiModelProperty(value = "记录ID列表")
    private List<Long> ids;

    @NotNull(message = "审核状态不能为空")
    @ApiModelProperty(value = "审核状态（1=通过, 2=拒绝）")
    private Integer auditStatus;
}
