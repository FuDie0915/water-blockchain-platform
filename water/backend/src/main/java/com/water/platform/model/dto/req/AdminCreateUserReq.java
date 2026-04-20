package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@ApiModel(description = "管理员创建用户参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminCreateUserReq {
    @ApiModelProperty(value = "账号", required = true)
    @NotBlank
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank
    private String userPassword;

    @ApiModelProperty(value = "用户姓名", required = true)
    @NotBlank
    private String userName;

    @ApiModelProperty(value = "角色类型：admin/farmers/manager", required = true)
    @NotBlank
    private String userRole;

    @ApiModelProperty(value = "扩展信息(JSON)")
    private String extInfo;
}
