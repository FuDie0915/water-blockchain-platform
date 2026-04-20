package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "修改用药记录请求（仅待审核状态可修改）")
@Data
public class MedicineUpdateReq {

    @NotNull(message = "记录ID不能为空")
    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "用药日期")
    private Date medicineDate;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "药品通用名")
    private String medicineName;

    @ApiModelProperty(value = "用药目的（预防/治疗/消毒）")
    private String purpose;

    @ApiModelProperty(value = "用药剂量")
    private String dosage;

    @ApiModelProperty(value = "休药期开始")
    private Date withdrawalStart;

    @ApiModelProperty(value = "休药期结束")
    private Date withdrawalEnd;

    @ApiModelProperty(value = "备注")
    private String remark;
}
