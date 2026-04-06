package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author ：devon
 * @date ：2024/10/10 15:47
 * @description：用户创建输入参数
 * @version: 1.0
 */
@ApiModel(description = "用户创建输入参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreateReq {
    @ApiModelProperty(value = "账号", required = true)
    @NotBlank
    private String userAccount;

    @ApiModelProperty(value = "密码")
    private String userPassword;

    @ApiModelProperty(value = "姓名")
    private String userName;

    @ApiModelProperty(value = "角色类型(admin/company/manager)")
    private String userRole;
}
