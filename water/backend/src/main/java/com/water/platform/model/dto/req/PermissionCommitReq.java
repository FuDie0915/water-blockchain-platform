package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author ：devon
 * @date ：2024/11/14 14:13
 * @description：企业排污许可证申请入参
 * @version: 1.0
 */
@ApiModel(description = "企业排污许可证申请入参")
@Data
public class PermissionCommitReq {

    @ApiModelProperty(value = "许可证图像路径")
    @NotBlank(message = "许可证图像路径不能为空")
    private String imageUrl;
}
