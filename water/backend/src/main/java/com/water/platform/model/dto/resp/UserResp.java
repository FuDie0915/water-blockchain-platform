package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ：devon
 * @date ：2024/10/10 16:18
 * @description：用户信息输出参数
 * @version: 1.0
 */
@ApiModel(description = "用户信息输出参数")
@Data
public class UserResp {
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "账号")
    private String userAccount;


    @ApiModelProperty(value = "名称(用户名称、企业名称、监管局名称)")
    private String userName;

    @ApiModelProperty(value = "角色类型", example = "admin、farmers、manager")
    private String userRole;

    @ApiModelProperty(value = "区块链账户地址", example = "0x0")
    private String accountAddress;

    @ApiModelProperty(value = "用户状态：0=禁用，1=启用")
    private Integer userStatus;

    @ApiModelProperty(value = "扩展信息(JSON)")
    private String extInfo;
}
