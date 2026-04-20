package com.water.platform.model.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author ：devon
 * @date ：2024/11/16 11:44
 * @description：水质步骤1信息输出模型
 * @version: 1.0
 */
@ApiModel(description = "水质步骤1信息输出模型")
@Data
@AllArgsConstructor
public class WaterStep1InfoResp {

    @ApiModelProperty(value = "养殖户账号")
    private String companyAccount;
    @ApiModelProperty(value = "养殖户区块链账户地址")
    private String companyAddress;
    @ApiModelProperty(value = "监管局账号")
    private String managerAccount;
    @ApiModelProperty(value = "监管局区块链账户地址")
    private String managerAddress;
}
