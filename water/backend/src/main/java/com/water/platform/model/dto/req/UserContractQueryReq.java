package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author ：devon
 * @date ：2024/11/12 15:04
 * @description：用户合约查询入参
 * @version: 1.0
 */
@ApiModel(description = "用户合约查询入参")
@Data
@AllArgsConstructor
public class UserContractQueryReq {
    @ApiModelProperty(value = "合约类型", required = true)
    @NotBlank(message = "合约类型不能为空")
    private String contractType;
}
