package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@ApiModel(description = "管理员修改用户状态参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserStatusUpdateReq {
    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull
    private Long userId;

    @ApiModelProperty(value = "用户状态：0=禁用，1=启用", required = true)
    @NotNull
    private Integer userStatus;
}
