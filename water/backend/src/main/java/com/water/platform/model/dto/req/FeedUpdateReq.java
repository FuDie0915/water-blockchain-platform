package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

@ApiModel(description = "修改投喂记录请求（仅待审核状态可修改）")
@Data
public class FeedUpdateReq {

    @NotNull(message = "记录ID不能为空")
    @ApiModelProperty(value = "记录ID")
    private Long id;

    @ApiModelProperty(value = "塘号")
    private Long pondId;

    @ApiModelProperty(value = "投喂日期")
    private Date feedDate;

    @ApiModelProperty(value = "饲料品牌")
    private String feedBrand;

    @ApiModelProperty(value = "投喂用量（kg）")
    private Double feedAmount;

    @ApiModelProperty(value = "责任人")
    private String manager;

    @ApiModelProperty(value = "备注")
    private String remark;
}
