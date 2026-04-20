package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @description：用户注册输入参数
 */
@ApiModel(description = "用户注册输入参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegisterReq {
    @ApiModelProperty(value = "账号", required = true)
    @NotBlank
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank
    private String userPassword;

    @ApiModelProperty(value = "扩展信息(JSON)")
    private String extInfo;

    @ApiModelProperty(value = "验证码key", required = true)
    @NotBlank
    private String captchaKey;

    @ApiModelProperty(value = "验证码", required = true)
    @NotBlank
    private String captchaCode;
}
