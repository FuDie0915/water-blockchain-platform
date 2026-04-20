package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "审核类型统计")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditTypeStat {

    @ApiModelProperty(value = "待审核数")
    private Long pending;

    @ApiModelProperty(value = "已通过数")
    private Long approved;

    @ApiModelProperty(value = "已拒绝数")
    private Long rejected;

    public static AuditTypeStat of(long pending, long approved, long rejected) {
        return new AuditTypeStat(pending, approved, rejected);
    }
}
