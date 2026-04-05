package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ：devon
 * @date ：2024/10/10 16:26
 * @description：用户登录输出参数
 * @version: 1.0
 */
@ApiModel(description = "用户登录输出参数")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResp {

    @ApiModelProperty(value = "用户信息输出")
    private UserResp userResp;

    @ApiModelProperty(value = "token")
    private String token;

}
