package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "管理员看板数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResp {

    @ApiModelProperty(value = "用户总数")
    private Long userTotal;

    @ApiModelProperty(value = "养殖户数")
    private Long farmerCount;

    @ApiModelProperty(value = "监管局数")
    private Long managerCount;

    @ApiModelProperty(value = "管理员数")
    private Long adminCount;

    @ApiModelProperty(value = "已上链水质数据条数")
    private Long onChainCount;

    @ApiModelProperty(value = "已上链许可证数（审核通过）")
    private Long certOnChainCount;

    @ApiModelProperty(value = "公告总数")
    private Long announcementCount;

    @ApiModelProperty(value = "待审核许可证数")
    private Long pendingCertCount;
}
