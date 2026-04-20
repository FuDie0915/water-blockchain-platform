package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "修改收获记录请求（仅待审核状态可修改）")
@Data
public class HarvestUpdateReq {

    @NotNull(message = "记录ID不能为空")
    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "收获日期")
    private Date harvestDate;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "收获批次")
    private String batchNo;

    @ApiModelProperty(value = "品种规格")
    private String spec;

    @ApiModelProperty(value = "总收获重量（kg）")
    private Double totalWeight;

    @ApiModelProperty(value = "成活率（%）")
    private Double survivalRate;

    @ApiModelProperty(value = "收购方信息")
    private String buyerInfo;

    @ApiModelProperty(value = "质量检测结果")
    private String qualityResult;

    @ApiModelProperty(value = "备注")
    private String remark;
}
