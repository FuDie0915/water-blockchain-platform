package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * @author ：devon
 * @date ：2024/11/12 15:12
 * @description：用户合约绑定入参
 * @version: 1.0
 */
@ApiModel(description = "用户合约绑定入参")
@Data
public class UserContractCreateReq {
    @ApiModelProperty(value = "合约类型", required = true)
    @NotBlank(message = "合约类型不能为空")
    private String contractType;

    @ApiModelProperty(value = "合约名称", required = true)
    @NotBlank(message = "合约名称不能为空")
    private String contractName;

    @ApiModelProperty(value = "合约地址", required = true)
    @NotBlank(message = "合约地址不能为空")
    private String contractAddress;

    @ApiModelProperty(value = "合约abi", required = true)
    @NotBlank(message = "合约abi不能为空")
    private String contractAbi;

    @ApiModelProperty(value = "合约bin", required = true)
    @NotBlank(message = "合约bin不能为空")
    private String contractBin;
}
