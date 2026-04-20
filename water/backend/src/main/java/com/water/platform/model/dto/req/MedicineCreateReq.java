package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "新增用药记录请求")
@Data
public class MedicineCreateReq {

    @NotNull(message = "塘号不能为空")
    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @NotNull(message = "用药日期不能为空")
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
