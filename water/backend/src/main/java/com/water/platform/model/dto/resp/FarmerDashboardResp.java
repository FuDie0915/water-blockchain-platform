package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "养殖户看板数据")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FarmerDashboardResp {

    @ApiModelProperty(value = "养殖池总数")
    private Long pondCount;

    @ApiModelProperty(value = "养殖中数量")
    private Long pondActiveCount;

    @ApiModelProperty(value = "水质数据总量")
    private Long waterDataTotal;

    @ApiModelProperty(value = "异常数据量")
    private Long waterAbnormalCount;

    @ApiModelProperty(value = "异常率(%)")
    private Double waterAbnormalRate;

    @ApiModelProperty(value = "最近7天水质趋势")
    private List<WaterTrendItem> waterTrend;

    @ApiModelProperty(value = "待审核总数")
    private Long pendingAuditCount;

    @ApiModelProperty(value = "已通过总数")
    private Long approvedCount;

    @ApiModelProperty(value = "已拒绝总数")
    private Long rejectedCount;

    @ApiModelProperty(value = "苗种记录数")
    private Long seedRecordCount;

    @ApiModelProperty(value = "投喂记录数")
    private Long feedRecordCount;

    @ApiModelProperty(value = "用药记录数")
    private Long medicineRecordCount;

    @ApiModelProperty(value = "收获记录数")
    private Long harvestRecordCount;
}
