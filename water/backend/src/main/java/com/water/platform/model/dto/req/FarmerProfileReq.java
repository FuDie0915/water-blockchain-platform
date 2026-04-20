package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "养殖户扩展信息")
@Data
public class FarmerProfileReq {
    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "养殖场名称")
    private String farmName;

    @ApiModelProperty(value = "养殖场地址")
    private String farmAddress;
}
