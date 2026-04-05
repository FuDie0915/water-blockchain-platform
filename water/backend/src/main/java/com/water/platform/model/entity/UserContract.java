package com.water.platform.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author ：devon
 * @date ：2024/11/12 14:34
 * @description：用户合约表
 * @version: 1.0
 */
@TableName(value = "user_contract")
@ApiModel("用户合约模型")
@Data
public class UserContract {

    private Long userId;

    private String contractType;

    private String contractName;

    private String contractAddress;

    private String contractAbi;

    private String contractBin;
}
