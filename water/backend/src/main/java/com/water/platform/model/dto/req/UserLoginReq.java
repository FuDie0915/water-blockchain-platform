package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ：devon
 * @date ：2024/10/10 16:21
 * @description：登录输入参数
 * @version: 1.0
 */
@ApiModel(description = "登录输入参数")
@Data
public class UserLoginReq {
    @ApiModelProperty(value = "账号", required = true)
    @NotBlank
    private String userAccount;
    @ApiModelProperty(value = "密码", required = true)
    @NotBlank
    private String userPassword;
    @ApiModelProperty(value = "登录类型(0企业、1监管局)", required = true)
    @NotNull
    private Integer loginType;
}
