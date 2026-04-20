package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author ：devon
 * @date ：2024/11/14 22:19
 * @description：企业许可证输出参数
 * @version: 1.0
 */
@ApiModel(description = "企业许可证输出参数")
@Data
public class CompanyCertResp {

    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "企业名称")
    private String companyName;

    @ApiModelProperty(value = "排污许可证图像路径")
    private String imageUrl;

    @ApiModelProperty(value = "状态(0:待审批、1:通过并上链、2:不通过)")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "是否在链上")
    private Boolean isOnChain;
}
