package com.water.platform.model.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "用户修改个人信息参数")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserProfileUpdateReq {
    @ApiModelProperty(value = "用户姓名")
    private String userName;

    @ApiModelProperty(value = "旧密码（修改密码时必填）")
    private String oldPassword;

    @ApiModelProperty(value = "新密码")
    private String userPassword;

    @ApiModelProperty(value = "扩展信息(JSON)")
    private String extInfo;
}
