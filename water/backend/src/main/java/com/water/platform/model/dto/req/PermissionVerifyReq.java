package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ：devon
 * @date ：2024/11/14 14:13
 * @description：监管局审批排污许可证入参
 * @version: 1.0
 */
@ApiModel(description = "监管局审批排污许可证入参")
@Data
public class PermissionVerifyReq {

    @ApiModelProperty(value = "ID", example = "1")
    @NotNull(message = "id不能为空")
    private Long id;

    @ApiModelProperty(value = "状态(1:通过并上链、2:不通过)")
    @NotBlank(message = "状态")
    @Min(1)
    @Max(2)
    private Integer status;
}
