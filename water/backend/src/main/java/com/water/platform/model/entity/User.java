package com.water.platform.model.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author ：devon
 * @date ：2024/10/10 15:45
 * @description：用户模型 与mysql表对应
 * @version: 1.0
 */
@TableName(value = "user")
@ApiModel(description = "用户模型")
@Data
public class User {

    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @ApiModelProperty(value = "账号")
    private String userAccount;

    @ApiModelProperty(value = "密码")
    private String userPassword;

    @ApiModelProperty(value = "名称(用户名称、企业名称、监管局名称)")
    private String userName;

    @ApiModelProperty(value = "角色类型", example = "admin、company、manager")
    private String userRole;

    @ApiModelProperty(value = "区块链账户地址", example = "0x0")
    private String accountAddress;

    @ApiModelProperty(value = "区块链账户私钥")
    private String privateKey;
}

