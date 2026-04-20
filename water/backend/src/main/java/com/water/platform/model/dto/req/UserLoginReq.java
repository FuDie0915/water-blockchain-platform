package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * @author ：devon
 * @date ：2024/10/10 16:21
 * @description：登录输入参数
 * @version: 1.0
 */
@ApiModel(description = "登录输入参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginReq {
    @ApiModelProperty(value = "账号", required = true)
    @NotBlank
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank
    private String userPassword;
}
