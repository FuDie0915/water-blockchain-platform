package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "绑定关系响应")
@Data
public class BindingResp {
    @ApiModelProperty(value = "绑定记录ID")
    private Long id;

    @ApiModelProperty(value = "监管局用户ID")
    private Long managerId;

    @ApiModelProperty(value = "监管局名称")
    private String managerName;

    @ApiModelProperty(value = "养殖户用户ID")
    private Long farmerId;

    @ApiModelProperty(value = "养殖户名称")
    private String farmerName;

    @ApiModelProperty(value = "状态（0=待审核, 1=已通过, 2=已拒绝）")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
