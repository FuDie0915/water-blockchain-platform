package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "管理员修改用户信息参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminUserUpdateReq {
    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "账号")
    private String userAccount;

    @ApiModelProperty(value = "扩展信息(JSON)")
    private String extInfo;
}
